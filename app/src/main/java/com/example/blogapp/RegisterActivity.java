//package com.example.blogapp;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.util.Log;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.blogapp.databinding.ActivityRegisterBinding;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//
//public class RegisterActivity extends AppCompatActivity {
//
//    ActivityRegisterBinding binding;
//    FirebaseAuth auth;
//    DatabaseReference databaseReference;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//
//        auth = FirebaseAuth.getInstance();
//        databaseReference = FirebaseDatabase.getInstance().getReference("Users"); // Reference to "Users" node in Firebase
//
//        // Handle "Register" button click
//        binding.btnRegister.setOnClickListener(v -> registerUser());
//
//        // Handle "Login" button click
//        binding.btnLogin.setOnClickListener(v -> navigateToLogin());
//    }
//
//    private void registerUser() {
//        String name = binding.etName.getText().toString().trim();
//        String email = binding.etEmail.getText().toString().trim();
//        String password = binding.etPassword.getText().toString().trim();
//        String rePassword = binding.etRePassword.getText().toString().trim();
//
//        // Validate input fields
//        if (TextUtils.isEmpty(name)) {
//            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (TextUtils.isEmpty(email)) {
//            Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (TextUtils.isEmpty(password)) {
//            Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (!password.equals(rePassword)) {
//            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Create the user with email and password
//        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                // Registration successful
//                Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();
//
//                // Save user data to Firebase Realtime Database
//                saveUserToDatabase(name, email);
//
//                navigateToSplash();
//            } else {
//                // Registration failed
//                Toast.makeText(this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void saveUserToDatabase(String name, String email) {
//        String userId = auth.getCurrentUser().getUid();
//
//        Log.d("RegisterActivity", "User ID: " + userId);
//        Log.d("RegisterActivity", "Email: " + email);
//
//        User user = new User(userId, name, email);
//
//        databaseReference.child(userId).setValue(user)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        Log.d("RegisterActivity", "User data saved successfully!");
//                        Toast.makeText(this, "User data saved successfully!", Toast.LENGTH_SHORT).show();
//                    } else {
//                        Log.e("RegisterActivity", "Failed to save user data: " + task.getException().getMessage());
//                        Toast.makeText(this, "Failed to save user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
//
//    private void navigateToSplash() {
//        Intent intent = new Intent(this, SplashActivity.class);
//        startActivity(intent);
//        finish();
//    }
//
//    private void navigateToLogin() {
//        finish(); // Closes RegisterActivity and returns to SplashActivity
//    }
//
//    // Define a User class to hold user data
//    public static class User {
//        public String userId;
//        public String name;
//        public String email;
//
//        public User() {
//            // Default constructor required for calls to DataSnapshot.getValue(User.class)
//        }
//
//        public User(String userId, String name, String email) {
//            this.userId = userId;
//            this.name = name;
//            this.email = email;
//        }
//    }
//}


package com.example.blogapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.blogapp.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

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
        String password = binding.etPassword.getText().toString().trim();
        String rePassword = binding.etRePassword.getText().toString().trim();

        // Validate input fields
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(rePassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the user with email and password
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Registration successful
                Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();

                // Save user data to Firestore
                saveUserToFirestore(name, email);

                navigateToSplash();
            } else {
                // Registration failed
                Toast.makeText(this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserToFirestore(String name, String email) {
        String userId = auth.getCurrentUser().getUid();

        Log.d("RegisterActivity", "User ID: " + userId);
        Log.d("RegisterActivity", "Email: " + email);

        User user = new User(userId, name, email);

        // Firestore reference to "users" collection, using userId as document ID
        firestore.collection("users").document(userId)
                .set(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("RegisterActivity", "User data saved successfully!");
                        Toast.makeText(this, "User data saved successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("RegisterActivity", "Failed to save user data: " + task.getException().getMessage());
                        Toast.makeText(this, "Failed to save user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
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

        public User() {
            // Default constructor required for Firestore deserialization
        }

        public User(String userId, String name, String email) {
            this.userId = userId;
            this.name = name;
            this.email = email;
        }
    }
}
