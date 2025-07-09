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
            // Đã đăng nhập → vào Homepage
            startActivity(new Intent(this, MainActivity.class));
        } else {
            // Chưa đăng nhập → vào Login
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }
}
