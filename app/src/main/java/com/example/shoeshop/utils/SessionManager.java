package com.example.shoeshop.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.shoeshop.Interface.NewProductListener; // Import NewProductListener
import com.example.shoeshop.activities.LoginActivity;
import com.example.shoeshop.models.Product;
import com.example.shoeshop.models.ProductResponse; // Import ProductResponse
import com.example.shoeshop.network.ApiClient; // Import ApiClient
import com.example.shoeshop.network.ApiService; // Import ApiService
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.Instant; // Import Instant
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call; // Import Call
import retrofit2.Callback; // Import Callback
import retrofit2.Response; // Import Response

public class SessionManager {
    private static final String PREF_NAME = "ShoeShopSession";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_PHONE = "phoneNumber";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_LAST_PRODUCT_CHECK_TIME = "last_product_check_time";
    private static final String KEY_HAS_NEW_PRODUCTS = "has_new_products";
    private static final String KEY_NEW_PRODUCTS_LIST = "new_products_list";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;
    private Gson gson;
    private ApiService apiService; // Khai báo ApiService
    private NewProductListener newProductListener; // Listener để thông báo

    public SessionManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        gson = new Gson();
        apiService = ApiClient.getClient().create(ApiService.class); // Khởi tạo ApiService
    }

    // Setter cho listener
    public void setNewProductListener(NewProductListener listener) {
        this.newProductListener = listener;
    }

    public void saveSession(String token, String userId, String name, String email, String phoneNumber,String role ) {
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_PHONE, phoneNumber);
        editor.putString(KEY_USER_ROLE, role);
        Log.d("SessionManager", "Saving session data:");
        Log.d("SessionManager", "Token: " + token);
        Log.d("SessionManager", "User ID: " + userId);
        Log.d("SessionManager", "User Name: " + name);
        Log.d("SessionManager", "User Email: " + email);
        Log.d("SessionManager", "User Phone: " + phoneNumber);
        Log.d("SessionManager", "User Role: " + role);
        editor.apply();
    }

    public void saveChatSessionIdForUser(String userId, String chatSessionId) {
        if (userId != null && !userId.isEmpty()) {
            editor.putString("chat_session_id_" + userId, chatSessionId);
            editor.apply();
            Log.d("SessionManager", "Chat Session ID saved for user " + userId + ": " + chatSessionId);
        } else {
            Log.e("SessionManager", "Cannot save chat session ID: userId is null or empty.");
        }
    }

    public String getChatSessionIdForUser(String userId) {
        if (userId != null && !userId.isEmpty()) {
            return sharedPreferences.getString("chat_session_id_" + userId, null);
        }
        return null;
    }

    public void clearChatSessionIdForUser(String userId) {
        if (userId != null && !userId.isEmpty()) {
            editor.remove("chat_session_id_" + userId);
            editor.apply();
            Log.d("SessionManager", "Chat Session ID cleared for user: " + userId);
        } else {
            Log.e("SessionManager", "Cannot clear chat session ID: userId is null or empty.");
        }
    }

    public void saveLastProductCheckTime(String timestamp) {
        editor.putString(KEY_LAST_PRODUCT_CHECK_TIME, timestamp);
        editor.apply();
        Log.d("SessionManager", "Last product check time saved: " + timestamp);
    }

    public String getLastProductCheckTime() {
        return sharedPreferences.getString(KEY_LAST_PRODUCT_CHECK_TIME, null);
    }

    public void setHasNewProducts(boolean hasNew) {
        editor.putBoolean(KEY_HAS_NEW_PRODUCTS, hasNew);
        editor.apply();
        Log.d("SessionManager", "Has new products flag set to: " + hasNew);
    }

    public boolean hasNewProducts() {
        return sharedPreferences.getBoolean(KEY_HAS_NEW_PRODUCTS, false);
    }

    public void saveNewProducts(List<Product> products) {
        String json = gson.toJson(products);
        editor.putString(KEY_NEW_PRODUCTS_LIST, json);
        editor.apply();
        Log.d("SessionManager", "Saved " + products.size() + " new products to preferences.");
    }

    public List<Product> getNewProducts() {
        String json = sharedPreferences.getString(KEY_NEW_PRODUCTS_LIST, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<Product>>() {}.getType();
        List<Product> products = gson.fromJson(json, type);
        return products != null ? products : new ArrayList<>();
    }

    public void clearNewProducts() {
        editor.remove(KEY_NEW_PRODUCTS_LIST);
        editor.apply();
        Log.d("SessionManager", "Cleared new products list from preferences.");
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getString(KEY_TOKEN, null) != null;
    }

    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }
    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }
    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, null);
    }

    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, null);
    }

    public String getUserPhone() {
        return sharedPreferences.getString(KEY_USER_PHONE, null);
    }
    public String getUserRole() {
        return sharedPreferences.getString(KEY_USER_ROLE, null);
    }

    public void logout() {
        editor.clear();
        editor.apply();
        Log.d("SessionManager", "User logged out. All session data cleared.");

        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    // MỚI: Phương thức kiểm tra sản phẩm mới từ API
    /**
     * Checks for new products from the API and updates SessionManager.
     * Notifies the NewProductListener if new products are found.
     * This method can be called from MainActivity or ChatAiFragment.
     */
    public void checkNewProductsFromApi() {
        String currentLastCheckedTime = getLastProductCheckTime();
        final String apiCallLastCheckedTime;

        if (currentLastCheckedTime == null || currentLastCheckedTime.isEmpty()) {
            // Use a very old timestamp for the first check if none is saved
            apiCallLastCheckedTime = "2024-01-01T00:00:00.000Z";
            Log.d("SessionManager", "Initial product check time for API call (first run/null): " + apiCallLastCheckedTime);
        } else {
            apiCallLastCheckedTime = currentLastCheckedTime;
            Log.d("SessionManager", "Using last checked time for API call: " + apiCallLastCheckedTime);
        }

        apiService.getRecentlyAddedProducts(apiCallLastCheckedTime).enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProductResponse productResponse = response.body();
                    List<Product> newProductsList = productResponse.getProducts();

                    Log.d("SessionManager", "API new product count: " + newProductsList.size() + " since " + apiCallLastCheckedTime);

                    if (newProductsList.size() > 0) {
                        setHasNewProducts(true);
                        saveNewProducts(newProductsList);
                        // Notify listener if there are new products
                        if (newProductListener != null) {
                            newProductListener.onNewProductsDetected(newProductsList.size());
                        }
                    } else {
                        // If no new products, ensure flag and list are reset
                        setHasNewProducts(false);
                        clearNewProducts();
                        // Also notify listener to potentially hide badge if it was showing
                        if (newProductListener != null) {
                            newProductListener.onNewProductsDetected(0);
                        }
                    }
                    // Always update the last checked time after a successful API call
                    saveLastProductCheckTime(Instant.now().toString());
                } else {
                    Log.e("SessionManager", "Failed to get recently added products: " + response.code() + " - " + response.message());
                    // Still update time to prevent continuous errors if API is consistently failing for old timestamp
                    saveLastProductCheckTime(Instant.now().toString());
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                Log.e("SessionManager", "Error checking recently added products", t);
                // Still update time to prevent continuous errors if API is consistently failing for old timestamp
                saveLastProductCheckTime(Instant.now().toString());
            }
        });
    }
}