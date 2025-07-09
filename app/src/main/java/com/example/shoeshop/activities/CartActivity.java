package com.example.shoeshop.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeshop.R;
import com.example.shoeshop.adapters.CartAdapter;
import com.example.shoeshop.models.CartItem;
import com.example.shoeshop.utils.CartStorage;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerViewCart;
    private Button btnPlaceOrder;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerViewCart = findViewById(R.id.recyclerViewCart);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);

        cartItems = CartStorage.loadCart(this);
        if (cartItems == null) cartItems = new ArrayList<>();

        cartAdapter = new CartAdapter(this, cartItems);
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCart.setAdapter(cartAdapter);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        btnPlaceOrder.setOnClickListener(v -> handlePlaceOrder());
    }

    private void handlePlaceOrder() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng đang trống", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Gọi API tạo đơn hàng ở đây nếu có

        Toast.makeText(this, "Đã đặt hàng " + cartItems.size() + " sản phẩm", Toast.LENGTH_SHORT).show();

        cartItems.clear();
        CartStorage.saveCart(this, cartItems);
        cartAdapter.notifyDataSetChanged();
    }
}
