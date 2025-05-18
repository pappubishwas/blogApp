package com.example.blogapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.blogapp.databinding.ActivityBlogDetailBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BlogDetail extends AppCompatActivity {
    private ActivityBlogDetailBinding binding;
    private String id, title, desc, count;
    private int n_count;

    private RecyclerView recyclerView;
    private EditText editTextComment;
    private Button buttonSend;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

    // ✅ Firebase references
    private DatabaseReference commentsRef;
    private DatabaseReference usersRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBlogDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        // ✅ Use Firebase Realtime Database instead of Firestore
        commentsRef = FirebaseDatabase.getInstance().getReference("Comments");
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        showdata();

        // ✅ Initialize UI elements using binding
        recyclerView = binding.recyclerViewComments;
        editTextComment = binding.editTextComment;
        buttonSend = binding.buttonSend;

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new SpaceItemDecoration(20));
        // ✅ Initialize comment list and adapter
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(commentList);
        recyclerView.setAdapter(commentAdapter);

        // Load comments from Realtime Database
        loadComments();
        binding.textView4.setOnClickListener(v -> {
            if (id != null && !id.isEmpty()) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // ✅ Get the blog document using the blog ID
                db.collection("Blogs").document(id).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String authorId = documentSnapshot.getString("userId");  // ✅ Fetch userId

                                if (authorId != null && !authorId.isEmpty()) {
                                    Log.d("DEBUG_ID", "Author ID: " + authorId); // Debug log
                                    Intent intent = new Intent(BlogDetail.this, BloggerProfileActivity.class);
                                    intent.putExtra("author_id", authorId); // ✅ Send the correct userId
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(BlogDetail.this, "Author ID not found!", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(BlogDetail.this, "Blog not found!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FirestoreError", "Error fetching blog details", e);
                            Toast.makeText(BlogDetail.this, "Error loading blog details", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(BlogDetail.this, "Blog ID is missing!", Toast.LENGTH_SHORT).show();
            }
        });




        // Send button click listener
        buttonSend.setOnClickListener(v -> postComment());
    }

    private void showdata() {
        id = getIntent().getStringExtra("id");

        FirebaseFirestore.getInstance().collection("Blogs").document(id)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null || !value.exists()) {
                        Toast.makeText(BlogDetail.this, "Failed to load blog", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Glide.with(getApplicationContext()).load(value.getString("img")).into(binding.imageView3);
                    binding.textView4.setText(Html.fromHtml("<font color='B7B7B7'>By </font> <font color='#000000'>" + value.getString("author")));
                    binding.textView5.setText(value.getString("tittle"));
                    binding.textView6.setText(value.getString("desc"));

                    title = value.getString("tittle");
                    desc = value.getString("desc");
                    count = value.contains("share_count") ? value.getString("share_count") : "0";

                    int i_count = Integer.parseInt(count);
                    n_count = i_count + 1;
                });


        // ✅ Share button click
        binding.floatingActionButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, title);
            intent.putExtra(Intent.EXTRA_TEXT, desc);
            startActivity(Intent.createChooser(intent, "Share Using"));

            HashMap<String, Object> map = new HashMap<>();
            map.put("share_count", String.valueOf(n_count));
            FirebaseFirestore.getInstance().collection("Blogs").document(id).update(map);
        });

        // ✅ Back button click
        binding.imageView4.setOnClickListener(v -> onBackPressed());
    }


private void loadComments() {
        commentsRef.child(id).orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Comment comment = data.getValue(Comment.class);
                    commentList.add(comment);
                }
                // ✅ Show newest comments first
                Collections.reverse(commentList);
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BlogDetail.this, "Failed to load comments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ Post Comment (Store in Realtime Database)
    private void postComment() {
        String commentText = editTextComment.getText().toString().trim();

        if (commentText.isEmpty()) {
            Toast.makeText(this, "Comment cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        buttonSend.setEnabled(false);  // Prevent multiple clicks

        String userId = auth.getCurrentUser().getUid();

        // ✅ Fetch username from Firestore instead of Realtime Database
        FirebaseFirestore.getInstance().collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Toast.makeText(BlogDetail.this, "User data not found!", Toast.LENGTH_SHORT).show();
                        buttonSend.setEnabled(true);
                        return;
                    }

                    String username = documentSnapshot.getString("name");  // ✅ Ensure "name" field exists
                    if (username == null) username = "Unknown User";  // Fallback

                    String timestamp = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());

                    String commentId = commentsRef.child(id).push().getKey();
                    if (commentId == null) {
                        Toast.makeText(BlogDetail.this, "Error generating comment ID!", Toast.LENGTH_SHORT).show();
                        buttonSend.setEnabled(true);
                        return;
                    }

                    // ✅ Store comment in Realtime Database
                    Map<String, Object> commentData = new HashMap<>();
                    commentData.put("username", username);
                    commentData.put("commentText", commentText);
                    commentData.put("timestamp", timestamp);
                    commentData.put("blogId", id);  // Store blog ID for filtering

                    commentsRef.child(id).child(commentId).setValue(commentData)
                            .addOnSuccessListener(unused -> {
                                editTextComment.setText("");
                                Toast.makeText(BlogDetail.this, "Comment posted!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(BlogDetail.this, "Failed to post comment", Toast.LENGTH_SHORT).show();
                            })
                            .addOnCompleteListener(task -> buttonSend.setEnabled(true)); // Re-enable button
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(BlogDetail.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                    buttonSend.setEnabled(true);
                });
    }

}
