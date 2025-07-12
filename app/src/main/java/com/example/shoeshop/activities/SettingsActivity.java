package com.example.shoeshop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.shoeshop.R;
import com.example.shoeshop.utils.ThemeHelper;

public class SettingsActivity extends AppCompatActivity {

    private boolean themeChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        /* ---------------- Switch Dark Mode ---------------- */
        SwitchCompat swDark = findViewById(R.id.switchDarkMode);
        swDark.setChecked(isDark());

        swDark.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ThemeHelper.toggleDarkMode(this, isChecked);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            recreate();
        });

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    /* ---------------- Utils ---------------- */
    private boolean isDark() {
        return getSharedPreferences("com.example.shoeshop_preferences", MODE_PRIVATE)
                .getBoolean("pref_dark_mode", false);
    }

    /* ---------------- Trả kết quả về MainActivity ---------------- */
    @Override
    public void onBackPressed() {
        returnToPreviousTab();
        super.onBackPressed();
    }

    private void returnToPreviousTab() {
        String fromTab = getIntent().getStringExtra("from_tab");
        Intent resultIntent = new Intent();
        resultIntent.putExtra("tab", fromTab);
        resultIntent.putExtra("theme_changed", themeChanged);
        setResult(RESULT_OK, resultIntent);
    }
}
