package com.example.shoeshop.activities;

import android.app.ProgressDialog;
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
import com.example.shoeshop.models.OrderRequest;
import com.example.shoeshop.models.OrderResponse;
import com.example.shoeshop.models.ProductOrderDetail;
import com.example.shoeshop.network.ApiClient;
import com.example.shoeshop.network.ApiService;
import com.example.shoeshop.utils.CartStorage;
import com.example.shoeshop.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerViewCart;
    private Button btnPlaceOrder;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems;
    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerViewCart = findViewById(R.id.recyclerViewCart);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        sessionManager = new SessionManager(this);
        apiService = ApiClient.getClient().create(ApiService.class);

        cartItems = CartStorage.loadCart(this);
        if (cartItems == null) cartItems = new ArrayList<>();

        cartAdapter = new CartAdapter(this, cartItems);
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCart.setAdapter(cartAdapter);

        btnPlaceOrder.setOnClickListener(v -> handlePlaceOrder());
    }

    private void handlePlaceOrder() {
        List<CartItem> selectedItems = new ArrayList<>();
        for (CartItem item : cartItems) {
            if (item.isSelected()) {
                selectedItems.add(item);
            }
        }

        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một sản phẩm để đặt hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = sessionManager.getToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo danh sách sản phẩm cho đơn hàng
        List<ProductOrderDetail> productOrderDetails = new ArrayList<>();
        for (CartItem item : selectedItems) {
            productOrderDetails.add(new ProductOrderDetail(item.getProductId(), item.getQuantity()));
        }

        OrderRequest orderRequest = new OrderRequest(null, productOrderDetails);


        // Gọi API đặt hàng
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Đang đặt hàng...");
        dialog.setCancelable(false);
        dialog.show();

        apiService.createOrder("Bearer " + token, orderRequest)
                .enqueue(new Callback<OrderResponse>() {
                    @Override
                    public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                        dialog.dismiss();

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(CartActivity.this,
                                    "Đặt hàng thành công!", Toast.LENGTH_LONG).show();

                            // Xóa các sản phẩm đã đặt khỏi giỏ
                            cartItems.removeAll(selectedItems);
                            CartStorage.saveCart(CartActivity.this, cartItems);
                            cartAdapter.notifyDataSetChanged();
                        } else {
                            String msg = (response.body() != null)
                                    ? response.body().getMessage()
                                    : "Lỗi khi đặt hàng";
                            Toast.makeText(CartActivity.this, "Thất bại: " + msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<OrderResponse> call, Throwable t) {
                        dialog.dismiss();
                        Toast.makeText(CartActivity.this,
                                "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
