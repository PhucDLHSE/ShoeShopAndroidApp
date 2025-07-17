package com.example.shoeshop.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.shoeshop.activities.LoginActivity;

public class SessionManager {
    private static final String PREF_NAME = "ShoeShopSession";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_PHONE = "phoneNumber";
    private static final String KEY_USER_ROLE = "user_role";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
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

    // --- Cập nhật phương thức để quản lý Chat Session ID theo UserID ---
    public void saveChatSessionIdForUser(String userId, String chatSessionId) {
        if (userId != null && !userId.isEmpty()) {
            editor.putString("chat_session_id_" + userId, chatSessionId); // Khóa động
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
    // ----------------------------------------------------

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

        // Chuyển hướng về màn hình đăng nhập
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}