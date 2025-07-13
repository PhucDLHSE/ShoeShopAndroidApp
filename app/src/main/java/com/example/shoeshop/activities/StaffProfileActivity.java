package com.example.shoeshop.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.example.shoeshop.R;
import com.example.shoeshop.utils.SessionManager;

public class StaffProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_profile);

        // Bind views
        ImageButton btnBack = findViewById(R.id.btnBack);
        TextView tvUserId = findViewById(R.id.tvUserId);
        TextView tvName = findViewById(R.id.tvName);
        TextView tvEmail = findViewById(R.id.tvEmail);
        TextView tvPhone = findViewById(R.id.tvPhone);
        TextView tvRole = findViewById(R.id.tvRole);
        Button btnLogout = findViewById(R.id.btnLogout);

        btnBack.setOnClickListener(v -> finish());

        // Load session data
        SessionManager sessionManager = new SessionManager(this);
        String userId = sessionManager.getUserId();
        String userName = sessionManager.getUserName();
        String userEmail = sessionManager.getUserEmail();
        String userPhone = sessionManager.getUserPhone();
        String role = sessionManager.getUserRole();

        // Bind data to views
        tvUserId.setText("User Id: "+userId);
        tvName.setText("User Name: "+userName);
        tvEmail.setText("User Email: "+userEmail);
        tvPhone.setText("User Phone: "+userPhone);
        tvRole.setText("Role: "+role);

        // Logout
        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            finish();
        });
    }
}