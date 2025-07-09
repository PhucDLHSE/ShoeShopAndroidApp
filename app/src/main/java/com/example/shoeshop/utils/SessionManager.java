package com.example.shoeshop.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.shoeshop.activities.LoginActivity;

public class SessionManager {
    private static final String PREF_NAME = "ShoeShopSession";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_PHONE = "phoneNumber";


    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveSession(String token, String userId, String name, String email, String phoneNumber ) {
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_PHONE, phoneNumber);
        editor.apply();
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
        return sharedPreferences.getString("phoneNumber", null);
    }

    public void logout() {
        editor.clear();
        editor.apply();
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}
