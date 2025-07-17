package com.example.shoeshop.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.shoeshop.R;
import com.example.shoeshop.fragments.ChatAiFragment;
import com.example.shoeshop.fragments.HomeFragment;
import com.example.shoeshop.fragments.MapFragment;
import com.example.shoeshop.fragments.UserProfileFragment;
import com.example.shoeshop.utils.CartStorage;
import com.example.shoeshop.utils.ThemeHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private int currentTabId = R.id.nav_home;

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
                        recreate();
                    } else {
                        bottomNavigationView.setSelectedItemId(currentTabId);
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
            currentTabId = id;
            if (id == R.id.nav_home) {
                loadFragment(new HomeFragment());
                return true;
            } else if (id == R.id.nav_profile) {
                loadFragment(new UserProfileFragment());
                return true;
            } else if (id == R.id.nav_chat) {
                loadFragment(new ChatAiFragment());
                return true;
            } else if (id == R.id.nav_map) {
                loadFragment(new MapFragment());
                return true;
            }
//           else if (id == R.id.nav_notifications) {
//                return true;
//            }
            return false;
        });

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