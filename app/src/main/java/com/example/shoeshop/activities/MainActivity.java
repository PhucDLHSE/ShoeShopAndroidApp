package com.example.shoeshop.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.shoeshop.R;
import com.example.shoeshop.fragments.HomeFragment;
import com.example.shoeshop.fragments.UserProfileFragment;
import com.example.shoeshop.utils.CartStorage;
import com.example.shoeshop.utils.ThemeHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    // 🔁 Biến để lưu tab hiện tại
    private int currentTabId = R.id.nav_home;

    // 📥 Nhận kết quả từ SettingsActivity
    private final ActivityResultLauncher<Intent> settingsLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    boolean themeChanged = data.getBooleanExtra("theme_changed", false);
                    String tab = data.getStringExtra("tab");

                    if (tab != null) {
                        if (tab.equals("profile")) {
                            currentTabId = R.id.nav_profile;
                        } else {
                            currentTabId = R.id.nav_home;
                        }
                    }

                    if (themeChanged) {
                        recreate(); // ⚡ Áp dụng dark/light theme mới
                    } else {
                        bottomNavigationView.setSelectedItemId(currentTabId); // về lại tab cũ
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CartStorage.getInstance().loadCartFromPrefs(getApplicationContext());

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            currentTabId = id; // Cập nhật tab hiện tại
            if (id == R.id.nav_home) {
                loadFragment(new HomeFragment());
                return true;
            } else if (id == R.id.nav_profile) {
                loadFragment(new UserProfileFragment());
                return true;
            } else if (id == R.id.nav_chat) { // 👈 Thêm xử lý cho Chat AI
                startActivity(new Intent(MainActivity.this, ChatAiActivity.class));
                return true;
            } else if (id == R.id.nav_map) { // 👈 Thêm xử lý cho Map
                startActivity(new Intent(MainActivity.this, MapActivity.class));
                return true;
            } else if (id == R.id.nav_notifications) {
                // Xử lý cho thông báo (nếu có fragment/activity riêng)
                return true;
            }
            return false;
        });

        // Đảm bảo rằng tab được chọn ban đầu chính xác
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(currentTabId);
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    public void openSettingsFromTab(String fromTab) {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra("from_tab", fromTab);
        settingsLauncher.launch(intent);
    }
}