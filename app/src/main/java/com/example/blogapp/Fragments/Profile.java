package com.example.blogapp.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.blogapp.R;
import com.example.blogapp.SplashActivity;
import com.example.blogapp.databinding.FragmentProfileBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class Profile extends Fragment {

    FragmentProfileBinding binding;
    GoogleSignInAccount account;
    GoogleSignInOptions signInOptions;
    GoogleSignInClient signInClient;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri; // To store selected image URI


    public Profile() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initvar();
        super.onViewCreated(view, savedInstanceState);
    }

    private void initvar() {
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        account = GoogleSignIn.getLastSignedInAccount(getContext());
        if (account != null) {
            binding.uName.setText(account.getDisplayName());
            binding.uEmail.setText(account.getEmail());
            Glide.with(getContext()).load(account.getPhotoUrl()).into(binding.profileDp);
        } else {
            fetchUserDataFromFirestore();
        }

        logoutuser();

        // ✅ Handle Long Press to Upload Image
        binding.profileDp.setOnLongClickListener(v -> {
            showImagePickerDialog();
            return true;
        });
    }

    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Upload Profile Picture")
                .setMessage("Do you want to choose an image from the gallery?")
                .setPositiveButton("Yes", (dialog, which) -> openGallery())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            uploadImageToFirebase(imageUri);
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("profile_pictures/" + userId + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();

                    // ✅ Update Firestore
                    firestore.collection("users").document(userId)
                            .update("profileImageUrl", imageUrl)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Profile Updated!", Toast.LENGTH_SHORT).show();
                                Glide.with(getContext()).load(imageUrl).into(binding.profileDp);
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show());

                }))
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Upload Failed!", Toast.LENGTH_SHORT).show());
    }

    private void fetchUserDataFromFirestore() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            firestore.collection("users").document(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            DocumentSnapshot document = task.getResult();
                            String username = document.getString("name");
                            String email = document.getString("email");
                            String profileImageUrl = document.getString("profileImageUrl");

                            binding.uName.setText(username);
                            binding.uEmail.setText(email);

                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                Glide.with(getContext()).load(profileImageUrl).into(binding.profileDp);
                            } else {
                                binding.profileDp.setImageResource(R.drawable.ic_launcher_background); // Default image
                            }
                        }
                    });
        }
    }

    private void logoutuser() {
        binding.btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

                signInClient = GoogleSignIn.getClient(getContext(), signInOptions);

                new AlertDialog.Builder(getActivity())
                        .setTitle("Log Out?")
                        .setMessage("Are you sure to logout from App?")
                        .setCancelable(false)
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseAuth.getInstance().signOut(); // Logout from Firebase
                                signInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        dialog.dismiss();
                                        startActivity(new Intent(getActivity().getApplicationContext(), SplashActivity.class));
                                        getActivity().finish();
                                    }
                                });
                            }
                        }).show();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
