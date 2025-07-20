package com.example.shoeshop.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shoeshop.R;
import com.example.shoeshop.utils.SessionManager;

public class UserInfoActivity extends AppCompatActivity {

    private EditText editName, editEmail, editPhone;
    private ImageButton btnSave;
    private ImageView btnBack;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        sessionManager = new SessionManager(getApplicationContext());

        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPhone = findViewById(R.id.editPhone);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        loadUserData();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String updatedName = editName.getText().toString();
                String updatedEmail = editEmail.getText().toString();
                String updatedPhone = editPhone.getText().toString();

                sessionManager.saveSession(
                        sessionManager.getToken(),
                        sessionManager.getUserId(),
                        updatedName,
                        updatedEmail,
                        updatedPhone,
                        sessionManager.getUserRole()
                );

                Toast.makeText(UserInfoActivity.this, "Thông tin đã được cập nhật", Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void loadUserData() {
        editName.setText(sessionManager.getUserName());
        editEmail.setText(sessionManager.getUserEmail());
        editPhone.setText(sessionManager.getUserPhone());
    }
}
