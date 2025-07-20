package com.example.shoeshop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
// import android.view.LayoutInflater; // Có thể bỏ nếu không dùng cho mục đích khác
// import android.view.View; // Có thể bỏ nếu không dùng cho mục đích khác
// import android.widget.TextView; // Có thể bỏ nếu không dùng cho mục đích khác
// import android.widget.Toast; // Có thể bỏ nếu không dùng cho mục đích khác

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.shoeshop.Interface.NewProductListener;
import com.example.shoeshop.R;
import com.example.shoeshop.fragments.ChatAiFragment;
import com.example.shoeshop.fragments.HomeFragment;
import com.example.shoeshop.fragments.MapFragment;
import com.example.shoeshop.fragments.UserProfileFragment;
import com.example.shoeshop.utils.CartStorage;
import com.example.shoeshop.utils.SessionManager;
import com.example.shoeshop.utils.ThemeHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.badge.BadgeDrawable;

// import java.util.List; // Không cần thiết nếu không sử dụng List trực tiếp ở đây

public class MainActivity extends AppCompatActivity implements NewProductListener {

    private BottomNavigationView bottomNavigationView;
    private int currentTabId = R.id.nav_home;
    private BadgeDrawable chatBadgeDrawable;
    private SessionManager sessionManager;
    private ChatAiFragment chatAiFragment;
    // private boolean isChatAiFragmentActive = false; // Không cần thiết

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
                        // Đảm bảo badge hiển thị đúng sau khi quay lại từ Settings
                        bottomNavigationView.setSelectedItemId(currentTabId);
                        // Khi quay lại, cần cập nhật badge lại dựa trên SessionManager
                        updateChatBadge(sessionManager.getNewProducts().size());
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
        sessionManager = new SessionManager(this);
        // RẤT QUAN TRỌNG: Đặt listener cho SessionManager ở MainActivity
        sessionManager.setNewProductListener(this);

        bottomNavigationView = findViewById(R.id.bottomNavigation);

        chatBadgeDrawable = bottomNavigationView.getOrCreateBadge(R.id.nav_chat);
        chatBadgeDrawable.setVisible(false);

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
                if (chatAiFragment == null) {
                    chatAiFragment = new ChatAiFragment();
                }
                loadFragment(chatAiFragment);

                // Khi người dùng click vào tab chat, KHÔNG ẩn badge ngay lập tức ở đây.
                // Việc ẩn badge sẽ do ChatAiFragment thông báo lại sau khi nó đã hiển thị sản phẩm.
                return true;
            } else if (id == R.id.nav_map) {
                loadFragment(new MapFragment());
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(currentTabId);
        } else {
            currentTabId = savedInstanceState.getInt("currentTabId", R.id.nav_home);
            bottomNavigationView.setSelectedItemId(currentTabId);
        }

        // Logic kiểm tra và hiển thị badge khi MainActivity được tạo/resume
        // 1. Kiểm tra trạng thái sản phẩm mới đã lưu trong SessionManager để hiển thị badge ban đầu
        int newProductCountOnStart = sessionManager.getNewProducts().size();

        Log.d("MainActivity", "onCreate (initial check): newProductCount=" + newProductCountOnStart);

        updateChatBadge(newProductCountOnStart); // Luôn cập nhật badge dựa trên số lượng đã lưu

        // 2. KÍCH HOẠT VIỆC KIỂM TRA SẢN PHẨM MỚI TỪ API NGAY SAU KHI MainActivity KHỞI TẠO
        // SessionManager sẽ gọi API và thông báo lại cho MainActivity qua NewProductListener
        sessionManager.checkNewProductsFromApi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Luôn kiểm tra sản phẩm mới khi MainActivity trở lại foreground
        sessionManager.checkNewProductsFromApi();

        // Cập nhật badge khi resume dựa trên trạng thái hiện tại của SessionManager
        updateChatBadge(sessionManager.getNewProducts().size());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentTabId", currentTabId);
    }

    private void loadFragment(Fragment fragment) {
        // Không cần gán chatAiFragment ở đây nếu bạn đã tạo nó khi nhấp vào tab và lưu vào biến thành viên.
        // if (fragment instanceof ChatAiFragment) {
        //     chatAiFragment = (ChatAiFragment) fragment;
        // }

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    public void openSettingsFromTab(String fromTab) {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra("from_tab", fromTab);
        settingsLauncher.launch(intent);
    }

    /**
     * Callback method from NewProductListener to update the UI.
     * Được gọi bởi SessionManager khi phát hiện sản phẩm mới từ API.
     * @param newProductCount Số lượng sản phẩm mới được phát hiện.
     */
    @Override
    public void onNewProductsDetected(int newProductCount) {
        Log.d("MainActivity", "onNewProductsDetected: Số lượng sản phẩm mới: " + newProductCount);

        // Cập nhật badge trên icon Chat AI.
        // Badge chỉ bị ẩn khi ChatAiFragment báo đã hiển thị.
        runOnUiThread(() -> updateChatBadge(newProductCount));
    }

    /**
     * Callback method from NewProductListener khi ChatAiFragment đã hiển thị sản phẩm mới trong chat.
     * Mục đích: để MainActivity ẩn badge.
     */
    @Override
    public void onNewProductsDisplayedInChat() { // Đổi tên phương thức cho rõ nghĩa
        Log.d("MainActivity", "onNewProductsDisplayedInChat: ChatAiFragment đã hiển thị sản phẩm mới.");
        // Khi ChatAiFragment báo đã hiển thị, ẩn badge
        runOnUiThread(() -> updateChatBadge(0)); // Ẩn badge
    }

    /**
     * Helper method to show/hide the badge on the chat icon.
     * @param count Số lượng sản phẩm mới. Nếu > 0, hiển thị badge với số lượng này. Nếu = 0, ẩn.
     */
    private void updateChatBadge(int count) {
        if (chatBadgeDrawable != null) {
            if (count > 0) {
                chatBadgeDrawable.setVisible(true);
                chatBadgeDrawable.setNumber(count);
                Log.d("MainActivity", "Chat badge set to: " + count);
            } else {
                // Chỉ ẩn badge nếu nó đang hiển thị và count là 0
                if (chatBadgeDrawable.isVisible()) {
                    chatBadgeDrawable.setVisible(false);
                    chatBadgeDrawable.clearNumber();
                    Log.d("MainActivity", "Chat badge hidden.");
                }
            }
        }
    }
}