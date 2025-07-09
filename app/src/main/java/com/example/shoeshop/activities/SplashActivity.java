package com.example.shoeshop.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shoeshop.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager sessionManager = new SessionManager(this);

        if (sessionManager.isLoggedIn()) {
            // Đã đăng nhập → kiểm tra role
            String role = sessionManager.getUserRole();
            if ("Staff".equalsIgnoreCase(role)) {
                startActivity(new Intent(this, StaffActivity.class));
            } else {
                // Mặc định là customer
                startActivity(new Intent(this, MainActivity.class));
            }
        } else {
            // Chưa đăng nhập → vào Login
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }
}
