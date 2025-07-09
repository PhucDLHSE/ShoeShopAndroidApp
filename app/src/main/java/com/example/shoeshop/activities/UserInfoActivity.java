package com.example.shoeshop.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shoeshop.R;

public class UserInfoActivity extends AppCompatActivity {

    private EditText editName, editEmail, editPhone;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPhone = findViewById(R.id.editPhone);
        btnSave = findViewById(R.id.btnSave);

        // TODO: Load dữ liệu người dùng hiện tại từ SharedPreferences hoặc API

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editName.getText().toString();
                String email = editEmail.getText().toString();
                String phone = editPhone.getText().toString();

                // TODO: Gửi dữ liệu lên server để cập nhật
                Toast.makeText(UserInfoActivity.this, "Đã lưu thông tin!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
