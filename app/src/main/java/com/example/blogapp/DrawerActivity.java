package com.example.blogapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.FragmentTransaction;
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

        // Set up NavigationView item selection listener
        binding.navigationView.setNavigationItemSelectedListener(this);
    }

    private void showUserProfile() {
        account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            Glide.with(this).load(account.getPhotoUrl()).into(binding.profileIcon);
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
        }

        binding.drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
