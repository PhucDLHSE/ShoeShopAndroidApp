package com.example.shoeshop.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.shoeshop.models.Product;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SharedPreferencesHelper {

    private static final String PREF_NAME = "cart_pref";
    private static final String CART_KEY = "cart_items";

    public static void addProductToCart(Context context, Product product) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String existingJson = prefs.getString(CART_KEY, null);

        List<Product> cart = new ArrayList<>();
        if (existingJson != null) {
            Type type = new TypeToken<List<Product>>() {}.getType();
            cart = gson.fromJson(existingJson, type);
        }

        cart.add(product);

        prefs.edit().putString(CART_KEY, gson.toJson(cart)).apply();
    }

    public static List<Product> getCartItems(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(CART_KEY, null);
        if (json == null) return new ArrayList<>();
        return new Gson().fromJson(json, new TypeToken<List<Product>>() {}.getType());
    }

    public static void clearCart(Context context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(CART_KEY)
                .apply();
    }
}
