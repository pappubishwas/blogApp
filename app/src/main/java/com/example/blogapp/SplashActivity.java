

package com.example.blogapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.blogapp.databinding.ActivitySplashBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
public class SplashActivity extends AppCompatActivity {

    ActivitySplashBinding binding;
    GoogleSignInOptions signInOptions;
    GoogleSignInClient signInClient;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupFirebase();
        setupSignin();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            // User is logged in, navigate to the homepage
            navigateToHome();
            return; // Stop further execution of onCreate
        }


        // Handle "Forgot Password?" click
        binding.tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());

        // Handle "Continue with Google" button click
        binding.btnGoogleSignIn.setOnClickListener(v -> signinWithGoogle());

        // Handle "Register" button click
        binding.btnRegister.setOnClickListener(v -> registerUser());

        // Handle "Login" button click
        binding.btnLogin.setOnClickListener(v -> loginUser());
    }

    private void setupFirebase() {
        auth = FirebaseAuth.getInstance();
    }

    private void setupSignin() {
        signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        signInClient = GoogleSignIn.getClient(this, signInOptions);
    }

    private void signinWithGoogle() {
        Intent intent = signInClient.getSignInIntent();
        startActivityForResult(intent, 100);
    }

    private void registerUser() {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    private void loginUser() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        // **Validation**
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.setError("Enter a valid email");
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 8) {
            binding.etPassword.setError("Enter a valid password (at least 8 characters)");
            return;
        }

        // **Login with Firebase**
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = auth.getCurrentUser();

                    if (user != null) {
                        if (user.isEmailVerified()) {
                            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                            navigateToHome();
                        } else {
                            Toast.makeText(this, "Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                            showResendVerificationDialog(user);
                            auth.signOut(); // Sign out user since they are not verified
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Login Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // **Show Resend Verification Dialog**
    private void showResendVerificationDialog(FirebaseUser user) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Email Not Verified")
                .setMessage("Would you like to resend the verification email?")
                .setPositiveButton("Resend", (dialog, which) -> sendVerificationEmail(user))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // **Resend Verification Email**
    private void sendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Verification email sent. Please check your inbox.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Failed to resend verification email.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                AuthCredential authCredential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                auth.signInWithCredential(authCredential).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                        navigateToHome();
                    } else {
                        Toast.makeText(this, "Login Failed!!", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void showForgotPasswordDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Reset Password");

        // Create an input field for email
        final EditText input = new EditText(this);
        input.setHint("Enter your email");
        builder.setView(input);

        // Set "Send Reset Link" button
        builder.setPositiveButton("Send", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Enter a valid email!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Send reset email
            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Password reset link sent! Check your email.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Failed to send reset email!", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Set "Cancel" button
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        // Show the dialog
        builder.show();
    }

    private void navigateToHome() {
        startActivity(new Intent(this, DrawerActivity.class));
        finish();
    }
}
