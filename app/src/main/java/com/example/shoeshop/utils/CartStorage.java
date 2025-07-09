package com.example.shoeshop.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.shoeshop.models.CartItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CartStorage {
    private static final String PREF_NAME = "cart_pref";
    private static final String KEY_CART = "cart_items";

    private static CartStorage instance;
    private List<CartItem> cartItems = new ArrayList<>();

    private CartStorage() {}

    public static CartStorage getInstance() {
        if (instance == null) {
            instance = new CartStorage();
        }
        return instance;
    }

    public void addToCart(CartItem newItem) {
        for (CartItem item : cartItems) {
            if (item.getProductId().equals(newItem.getProductId())) {
                item.setQuantity(item.getQuantity() + newItem.getQuantity());
                return;
            }
        }
        cartItems.add(newItem);
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void clearCart() {
        cartItems.clear();
    }

    public static void saveCart(Context context, List<CartItem> cartItems) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String json = new Gson().toJson(cartItems);
        editor.putString(KEY_CART, json);
        editor.apply();
    }

    public static List<CartItem> loadCart(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_CART, null);
        if (json != null) {
            Type type = new TypeToken<List<CartItem>>(){}.getType();
            return new Gson().fromJson(json, type);
        }
        return new ArrayList<>();
    }

    public void loadCartFromPrefs(Context context) {
        List<CartItem> savedItems = loadCart(context);
        if (savedItems != null) {
            cartItems = savedItems;
        }
    }

}
