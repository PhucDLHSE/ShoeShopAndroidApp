package com.example.shoeshop.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.shoeshop.R;
import com.example.shoeshop.fragments.HomeFragment;
import com.example.shoeshop.fragments.UserProfileFragment;
import com.example.shoeshop.utils.CartStorage;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CartStorage.getInstance().loadCartFromPrefs(getApplicationContext());
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                loadFragment(new HomeFragment());
                return true;
            } else if (id == R.id.nav_profile) {
                loadFragment(new UserProfileFragment());
                return true;
            } else if (id == R.id.nav_notifications) {
                // TODO: Replace with actual NotificationFragment
                return true;
            }
            return false;
        });

        // Default Fragment
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
