// ✅ ProductDetailActivity.java
package com.example.shoeshop.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shoeshop.R;
import com.example.shoeshop.adapters.FeedbackAdapter;
import com.example.shoeshop.adapters.FeedbackProductAdapter;
import com.example.shoeshop.models.CartItem;
import com.example.shoeshop.models.Feedback;
import com.example.shoeshop.models.OrderRequest;
import com.example.shoeshop.models.OrderResponse;
import com.example.shoeshop.models.Product;
import com.example.shoeshop.models.ProductOrderDetail;
import com.example.shoeshop.network.ApiClient;
import com.example.shoeshop.network.ApiService;
import com.example.shoeshop.network.FeedbackApiClient;
import com.example.shoeshop.utils.CartStorage;
import com.example.shoeshop.utils.SessionManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView imgProductDetail;
    private TextView tvProductName, tvProductPrice, tvProductSize, tvProductStock, tvProductDescription;
    private Button btnAddToCart, btnOrderNow;
    private Product currentProduct;
    private ApiService apiService;
    private RecyclerView rvFeedbackList;
    private FeedbackProductAdapter feedbackAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        imgProductDetail = findViewById(R.id.imgProductDetail);
        tvProductName = findViewById(R.id.tvProductName);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvProductSize = findViewById(R.id.tvProductSize);
        tvProductStock = findViewById(R.id.tvProductStock);
        tvProductDescription = findViewById(R.id.tvProductDescription);

        rvFeedbackList = findViewById(R.id.rvFeedbackList);
        rvFeedbackList.setLayoutManager(new LinearLayoutManager(this));
        feedbackAdapter = new FeedbackProductAdapter(this);
        rvFeedbackList.setAdapter(feedbackAdapter);

        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnOrderNow = findViewById(R.id.btnOrderNow);

        apiService = ApiClient.getClient().create(ApiService.class);

        String productId = getIntent().getStringExtra("productId");
        if (productId != null) {
            fetchProductDetail(productId);
        }

        btnAddToCart.setOnClickListener(v -> showAddToCartDialog());
        btnOrderNow.setOnClickListener(v -> showOrderNowDialog());

    }

    private void fetchProductDetail(String productId) {
        Call<Product> call = apiService.getProductById(productId);
        call.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentProduct = response.body();
                    displayProductDetails(currentProduct);
                    fetchFeedbackList(productId);

                } else {
                    Toast.makeText(ProductDetailActivity.this, "Không thể tải sản phẩm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                Toast.makeText(ProductDetailActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayProductDetails(Product product) {
        tvProductName.setText(product.getProductName());

        String formattedPrice = NumberFormat.getInstance(new Locale("vi", "VN"))
                .format(product.getPrice()) + "đ";
        tvProductPrice.setText("Giá: " + formattedPrice);

        tvProductSize.setText("Size: " + product.getSize());
        tvProductStock.setText("Còn lại: " + product.getStockQuantity() + " sản phẩm");
        tvProductDescription.setText(product.getDescription());

        Glide.with(this)
                .load(product.getImageUrl())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.image_error)
                .into(imgProductDetail);
    }
    private void showAddToCartDialog() {
        if (currentProduct == null) return;

        int stock = currentProduct.getStockQuantity();
        if (stock <= 0) {
            Toast.makeText(this, "Sản phẩm đã hết hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_order_quantity, null);
        TextView tvQuantity = dialogView.findViewById(R.id.tvQuantity);
        Button btnIncrease = dialogView.findViewById(R.id.btnIncrease);
        Button btnDecrease = dialogView.findViewById(R.id.btnDecrease);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmOrder);
        btnConfirm.setText("Thêm vào giỏ hàng");

        final int[] quantity = {1};
        tvQuantity.setText(String.valueOf(quantity[0]));

        btnIncrease.setOnClickListener(view -> {
            if (quantity[0] < stock) {
                quantity[0]++;
                tvQuantity.setText(String.valueOf(quantity[0]));
            } else {
                Toast.makeText(this, "Vượt quá số lượng tồn kho", Toast.LENGTH_SHORT).show();
            }
        });

        btnDecrease.setOnClickListener(view -> {
            if (quantity[0] > 1) {
                quantity[0]--;
                tvQuantity.setText(String.valueOf(quantity[0]));
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnConfirm.setOnClickListener(view -> {
            CartItem item = new CartItem(
                    currentProduct.getProductID(),
                    currentProduct.getProductName(),
                    currentProduct.getImageUrl(),
                    currentProduct.getPrice(),
                    quantity[0]
            );
            CartStorage.getInstance().addToCart(item);
            CartStorage.saveCart(getApplicationContext(), CartStorage.getInstance().getCartItems());
            Toast.makeText(this, "Đã thêm " + quantity[0] + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showOrderNowDialog() {
        if (currentProduct == null) return;

        int stock = currentProduct.getStockQuantity();
        if (stock <= 0) {
            Toast.makeText(this, "Sản phẩm đã hết hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_order_quantity, null);
        TextView tvQuantity = dialogView.findViewById(R.id.tvQuantity);
        Button btnIncrease = dialogView.findViewById(R.id.btnIncrease);
        Button btnDecrease = dialogView.findViewById(R.id.btnDecrease);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmOrder);
        btnConfirm.setText("Đặt hàng");

        final int[] quantity = {1};
        tvQuantity.setText(String.valueOf(quantity[0]));

        btnIncrease.setOnClickListener(view -> {
            if (quantity[0] < stock) {
                quantity[0]++;
                tvQuantity.setText(String.valueOf(quantity[0]));
            } else {
                Toast.makeText(this, "Vượt quá số lượng tồn kho", Toast.LENGTH_SHORT).show();
            }
        });

        btnDecrease.setOnClickListener(view -> {
            if (quantity[0] > 1) {
                quantity[0]--;
                tvQuantity.setText(String.valueOf(quantity[0]));
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnConfirm.setOnClickListener(view -> {
            SessionManager sessionManager = new SessionManager(this);
            String token = sessionManager.getToken();

            if (token == null || token.isEmpty()) {
                Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                return;
            }

            List<ProductOrderDetail> orderItems = new ArrayList<>();
            orderItems.add(new ProductOrderDetail(
                    currentProduct.getProductID(),
                    quantity[0]
            ));

            OrderRequest request = new OrderRequest("", orderItems);

            apiService.createOrder("Bearer " + token, request).enqueue(new Callback<OrderResponse>() {
                @Override
                public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        OrderResponse res = response.body();
                        if (res.isSuccess()) {
                            Toast.makeText(ProductDetailActivity.this,
                                    "Đặt hàng thành công. Mã QR đã được tạo.",
                                    Toast.LENGTH_LONG).show();

                            // TODO: Nếu muốn hiển thị ảnh mã QR:
                            // showQrDialog(res.getQrCodeUrl());

                        } else {
                            Toast.makeText(ProductDetailActivity.this,
                                    "Thất bại: " + res.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ProductDetailActivity.this,
                                "Lỗi phản hồi từ server", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                }

                @Override
                public void onFailure(Call<OrderResponse> call, Throwable t) {
                    Toast.makeText(ProductDetailActivity.this,
                            "Lỗi kết nối: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
        });


        dialog.show();
    }
    private void fetchFeedbackList(String productId) {
        ApiService api = FeedbackApiClient.getClient().create(ApiService.class);

        api.getProductFeedback(productId).enqueue(new Callback<List<Feedback>>() {
            @Override
            public void onResponse(Call<List<Feedback>> call, Response<List<Feedback>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    feedbackAdapter.setData(response.body());
                } else {
                    Log.e("ProductDetail", "Response code: " + response.code());
                    feedbackAdapter.setData(null);
                }
            }

            @Override
            public void onFailure(Call<List<Feedback>> call, Throwable t) {
                t.printStackTrace();
                feedbackAdapter.setData(null);
                Log.e("ProductDetail", "Lỗi tải feedback: " + t.getMessage());
            }
        });
    }


}
