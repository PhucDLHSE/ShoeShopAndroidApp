package com.example.shoeshop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.shoeshop.R;
import com.example.shoeshop.models.LoginRequest;
import com.example.shoeshop.models.LoginResponse;
import com.example.shoeshop.models.User;
import com.example.shoeshop.network.ApiClient;
import com.example.shoeshop.network.ApiService;
import com.example.shoeshop.utils.CartStorage;
import com.example.shoeshop.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);

        apiService = ApiClient.getClient().create(ApiService.class);
        sessionManager = new SessionManager(this);

        loginButton.setOnClickListener(v -> performLogin());

        Button btnGoToRegister = findViewById(R.id.btnGoToRegister);
        btnGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

    }

    private void performLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        LoginRequest request = new LoginRequest(email, password);
        Call<LoginResponse> call = apiService.login(request);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginRes = response.body();
                    User user = response.body().getUser();

                    if (loginRes.getUser() != null && loginRes.getToken() != null) {
                        String name = loginRes.getUser().getName();
                        String email = loginRes.getUser().getEmail();
                        String token = loginRes.getToken();
                        String userId = loginRes.getUser().getUserID();
                        String phoneNumber = loginRes.getUser().getPhoneNumber();
                        String role = user.getRoleName();
                        sessionManager.saveSession(token, userId, name, email, phoneNumber, role);
                        CartStorage.getInstance().loadCartFromPrefs(getApplicationContext());
                        Toast.makeText(LoginActivity.this,
                                "Xin chào " + name,
                                Toast.LENGTH_SHORT).show();

                        Intent intent;
                        if("Staff".equals(role)) {
                            intent = new Intent(LoginActivity.this, StaffActivity.class);
                        }
                        else {
                            intent = new Intent(LoginActivity.this, MainActivity.class);
                        }
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        Toast.makeText(LoginActivity.this, "Dữ liệu phản hồi thiếu thông tin user/token", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Email hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
