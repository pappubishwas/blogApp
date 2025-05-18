package com.example.blogapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.blogapp.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    ActivityRegisterBinding binding;
    FirebaseAuth auth;
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();  // Initialize Firestore

        // Handle "Register" button click
        binding.btnRegister.setOnClickListener(v -> registerUser());

        // Handle "Login" button click
        binding.btnLogin.setOnClickListener(v -> navigateToLogin());
    }

    private void registerUser() {
        String name = binding.etName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String phone = binding.etPhoneNumber.getText().toString().trim();  // Get phone number
        String password = binding.etPassword.getText().toString().trim();
        String rePassword = binding.etRePassword.getText().toString().trim();

        // **Input Validation**
        if (TextUtils.isEmpty(name)) {
            binding.etName.setError("Name is required");
            return;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.setError("Enter a valid email");
            return;
        }

        if (TextUtils.isEmpty(phone) || phone.length() < 10 || !Patterns.PHONE.matcher(phone).matches()) {
            binding.etPhoneNumber.setError("Enter a valid phone number");
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 8 || !password.matches(".*[!@#$%^&*].*")) {
            binding.etPassword.setError("Password must be at least 8 characters and include a special character.");
            return;
        }

        if (!password.equals(rePassword)) {
            binding.etRePassword.setError("Passwords do not match");
            return;
        }

        // **Register user with Firebase Authentication**
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = auth.getCurrentUser();

                    if (user != null) {
                        // Send email verification
                        user.sendEmailVerification()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(this, "Verification email sent. Please check your inbox.", Toast.LENGTH_LONG).show();

                                        // Save user data to Firestore
                                        saveUserToFirestore(user.getUid(), name, email, phone);

                                        // Sign out the user until they verify their email
                                        auth.signOut();

                                        // Redirect to login
                                        navigateToLogin();
                                    } else {
                                        Toast.makeText(this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Registration Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // **Save User Data to Firestore**
    private void saveUserToFirestore(String userId, String name, String email, String phone) {
        User user = new User(userId, name, email, phone); // Create user object

        firestore.collection("users")
                .document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d("RegisterActivity", "User data saved successfully!");
                    Toast.makeText(this, "User data saved successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("RegisterActivity", "Error saving user data: " + e.getMessage());
                    Toast.makeText(this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToSplash() {
        Intent intent = new Intent(this, SplashActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToLogin() {
        finish(); // Closes RegisterActivity and returns to SplashActivity
    }

    // Define a User class to hold user data
    public static class User {
        public String userId;
        public String name;
        public String email;
        public String phone; // Add phone number field

        public User() {
            // Default constructor required for Firestore deserialization
        }

        public User(String userId, String name, String email, String phone) {
            this.userId = userId;
            this.name = name;
            this.email = email;
            this.phone = phone; // Assign phone number
        }
    }
}
