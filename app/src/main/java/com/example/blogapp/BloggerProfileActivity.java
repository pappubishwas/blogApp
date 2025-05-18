package com.example.blogapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
//
//public class BloggerProfileActivity extends AppCompatActivity {
//
//    private TextView nameTextView, emailTextView, followerCountTextView;
//    private Button followButton;
//
//    private FirebaseFirestore db;
//    private FirebaseAuth auth;
//    private FirebaseUser currentUser;
//
//    private String authorId;
//    private boolean isFollowing = false; // To track follow status
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_blogger_profile);
//
//        // Initialize Firestore & Firebase Auth
//        db = FirebaseFirestore.getInstance();
//        auth = FirebaseAuth.getInstance();
//        currentUser = auth.getCurrentUser();
//
//        // Initialize UI components
//        nameTextView = findViewById(R.id.u_name);
//        emailTextView = findViewById(R.id.u_email);
//        followerCountTextView = findViewById(R.id.follower);
//        followButton = findViewById(R.id.btn_follow);
//
//        // Get author ID from intent
//        authorId = getIntent().getStringExtra("author_id");
//
//        if (authorId != null) {
//            // Load blogger details & follow status
//            loadBloggerProfile();
//            checkFollowStatus();
//        }
//
//        // Handle Follow/Unfollow button click
//        followButton.setOnClickListener(v -> {
//            if (isFollowing) {
//                unfollowUser();
//            } else {
//                followUser();
//            }
//        });
//    }
//
//    private void loadBloggerProfile() {
//        db.collection("users").document(authorId).get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    if (documentSnapshot.exists()) {
//                        String name = documentSnapshot.getString("name");
//                        String email = documentSnapshot.getString("email");
//
//                        nameTextView.setText(name);
//                        emailTextView.setText(email);
//
//                        // Load followers count
//                        loadFollowerCount();
//                    }
//                })
//                .addOnFailureListener(e -> Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show());
//    }
//
//    private void loadFollowerCount() {
//        db.collection("followers").document(authorId).collection("user_followers")
//                .get()
//                .addOnSuccessListener(querySnapshot -> {
//                    int count = querySnapshot.size();
//                    followerCountTextView.setText("Followers: " + count);
//                });
//    }
//
//    private void checkFollowStatus() {
//        if (currentUser == null) return;
//
//        db.collection("followers").document(authorId).collection("user_followers")
//                .document(currentUser.getUid()).get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    if (documentSnapshot.exists()) {
//                        isFollowing = true;
//                        followButton.setText("Followed");
//                    } else {
//                        isFollowing = false;
//                        followButton.setText("Follow");
//                    }
//                });
//    }
//
//    private void followUser() {
//        if (currentUser == null) return;
//
//        // Store follower ID under author's `followers` collection
//        Map<String, Object> followData = new HashMap<>();
//        followData.put("userId", currentUser.getUid());
//
//        db.collection("followers").document(authorId)
//                .collection("user_followers").document(currentUser.getUid())
//                .set(followData)
//                .addOnSuccessListener(aVoid -> {
//                    isFollowing = true;
//                    followButton.setText("Followed");
//                    loadFollowerCount(); // Refresh follower count
//                })
//                .addOnFailureListener(e -> Toast.makeText(this, "Failed to follow", Toast.LENGTH_SHORT).show());
//    }
//
//    private void unfollowUser() {
//        if (currentUser == null) return;
//
//        db.collection("followers").document(authorId)
//                .collection("user_followers").document(currentUser.getUid())
//                .delete()
//                .addOnSuccessListener(aVoid -> {
//                    isFollowing = false;
//                    followButton.setText("Follow");
//                    loadFollowerCount(); // Refresh follower count
//                })
//                .addOnFailureListener(e -> Toast.makeText(this, "Failed to unfollow", Toast.LENGTH_SHORT).show());
//    }
//}



public class BloggerProfileActivity extends AppCompatActivity {

    private TextView nameTextView, emailTextView, followerCountTextView;
    private TextView followButton;


    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    private String authorId;
    private boolean isFollowing = false; // To track follow status

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blogger_profile);

        // Initialize Firestore & Firebase Auth
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        // Initialize UI components
        nameTextView = findViewById(R.id.u_name);
        emailTextView = findViewById(R.id.u_email);
        followerCountTextView = findViewById(R.id.follower);
        followButton = findViewById(R.id.btn_follow);

        // Get author ID from intent
        authorId = getIntent().getStringExtra("author_id");
        Log.d("DEBUG_PROFILE", "Received Author ID: " + authorId);

        if (authorId == null || authorId.isEmpty()) {
            Toast.makeText(this, "Error: Author ID is missing!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to follow users.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load blogger details & follow status
        loadBloggerProfile();
        checkFollowStatus();

        // Handle Follow/Unfollow button click
        followButton.setOnClickListener(v -> {
            if (isFollowing) {
                unfollowUser();
            } else {
                followUser();
            }
        });
    }

    private void loadBloggerProfile() {
        db.collection("users").document(authorId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");

                        nameTextView.setText(name);
                        emailTextView.setText(email);

                        loadFollowerCount(); // ✅ Load follower count
                    } else {
                        Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error fetching user", e);
                    Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
                });

    }

    private void loadFollowerCount() {
        db.collection("followers").document(authorId).collection("user_followers")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot.size();
                    followerCountTextView.setText("Followers: " + count);
                });
    }

    private void checkFollowStatus() {
        if (currentUser == null) return;

        db.collection("followers").document(authorId).collection("user_followers")
                .document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        isFollowing = true;
                        followButton.setText("Followed");
                    } else {
                        isFollowing = false;
                        followButton.setText("Follow");
                    }
                });
    }

    private void followUser() {
        if (currentUser == null) return;

        // Store follower ID under author's `followers` collection
        Map<String, Object> followData = new HashMap<>();
        followData.put("userId", currentUser.getUid());

        db.collection("followers").document(authorId)
                .collection("user_followers").document(currentUser.getUid())
                .set(followData)
                .addOnSuccessListener(aVoid -> {
                    isFollowing = true;
                    followButton.setText("Followed");
                    loadFollowerCount(); // Refresh follower count
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to follow", Toast.LENGTH_SHORT).show());
    }

    private void unfollowUser() {
        if (currentUser == null) return;

        db.collection("followers").document(authorId)
                .collection("user_followers").document(currentUser.getUid())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    isFollowing = false;
                    followButton.setText("Follow");
                    loadFollowerCount(); // Refresh follower count
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to unfollow", Toast.LENGTH_SHORT).show());
    }
}
