package com.example.blogapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.blogapp.Fragments.Blogs;
import com.example.blogapp.R;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import com.bumptech.glide.Glide;
import com.example.blogapp.databinding.ActivityDrawerBinding;
import com.example.blogapp.Fragments.Home;
import com.example.blogapp.Fragments.Profile;
import com.example.blogapp.Fragments.Publish;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class DrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    ActivityDrawerBinding binding;
    GoogleSignInAccount account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDrawerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupDrawer();
        showUserProfile();
    }


    private void setupDrawer() {
        // Initially load the Home fragment
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, new Home());
        fragmentTransaction.commit();

        // Handle navigation menu icon click
        binding.menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.drawer.openDrawer(Gravity.LEFT);
            }
        });

        binding.profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the Profile fragment
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frame_layout, new Profile());
                fragmentTransaction.commit();

                // Optionally, close the drawer after selecting the profile
                binding.drawer.closeDrawer(GravityCompat.START);
            }
        });

        // Set up NavigationView item selection listener
        binding.navigationView.setNavigationItemSelectedListener(this);
    }

    private void showUserProfile() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // ✅ Google Sign-In Check
        account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null && account.getPhotoUrl() != null) {
            Glide.with(this).load(account.getPhotoUrl()).into(binding.profileIcon);
            return;
        }

        // ✅ Firestore Realtime Listener
        if (currentUser != null) {
            String userId = currentUser.getUid();
            firestore.collection("users").document(userId)
                    .addSnapshotListener((documentSnapshot, e) -> {
                        if (e != null) return;
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                Glide.with(this).load(profileImageUrl).into(binding.profileIcon);
                            } else {
                                binding.profileIcon.setImageResource(R.drawable.side_nav_bar);
                            }
                        }
                    });
        } else {
            binding.profileIcon.setImageResource(R.drawable.side_nav_bar);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame_layout, new Home());
            fragmentTransaction.commit();
        } else if (id == R.id.nav_publish) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame_layout, new Publish());
            fragmentTransaction.commit();
        } else if (id == R.id.nav_profile) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame_layout, new Profile());
            fragmentTransaction.commit();
        }else if(id== R.id.nav_blogs){
            FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame_layout,new Blogs());
            fragmentTransaction.commit();
        }

        binding.drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
