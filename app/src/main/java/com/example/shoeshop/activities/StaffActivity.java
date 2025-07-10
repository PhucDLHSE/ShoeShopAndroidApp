package com.example.shoeshop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.shoeshop.R;
import com.example.shoeshop.fragments.StaffDeliveryFragment;
import com.example.shoeshop.fragments.StaffFeedbackFragment;
import com.example.shoeshop.fragments.StaffOrdersFragment;
import com.example.shoeshop.fragments.StaffProductFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class StaffActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff);

        // Profile icon click → ProfileActivity
        ImageView ivProfile = findViewById(R.id.ivProfile);
        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StaffActivity.this, StaffProfileActivity.class);
                startActivity(intent);
            }
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                Fragment selectedFragment;
                int id = item.getItemId();

                if (id == R.id.nav_orders) {
                    selectedFragment = new StaffOrdersFragment();
                } else if (id == R.id.nav_delivery) {
                    selectedFragment = new StaffDeliveryFragment();
                } else if (id == R.id.nav_product) {
                    selectedFragment = new StaffProductFragment();
                } else if (id == R.id.nav_feedback) {
                    selectedFragment = new StaffFeedbackFragment();
                } else {
                    // default fallback
                    selectedFragment = new StaffOrdersFragment();
                }

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, selectedFragment)
                        .commit();
                return true;
            }
        });

        // load default
        bottomNav.setSelectedItemId(R.id.nav_orders);
    }
}