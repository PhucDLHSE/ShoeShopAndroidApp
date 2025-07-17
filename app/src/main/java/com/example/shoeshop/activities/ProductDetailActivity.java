package com.example.shoeshop.activities;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
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
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView imgProductDetail, btnBack;
    private TextView tvProductName, tvProductPrice, tvProductSize, tvProductStock, tvProductDescription;
    private Button btnAddToCart, btnOrderNow;
    private RecyclerView rvFeedbackList;

    private Product currentProduct;
    private FeedbackProductAdapter feedbackAdapter;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        initViews();
        apiService = ApiClient.getClient().create(ApiService.class);

        String productId = getIntent().getStringExtra("productId");
        if (productId != null) {
            fetchProductDetail(productId);
        }

        btnAddToCart.setOnClickListener(v -> showAddToCartDialog());
        btnOrderNow.setOnClickListener(v -> showOrderNowDialog());
    }

    private void initViews() {
        imgProductDetail = findViewById(R.id.imgProductDetail);
        tvProductName = findViewById(R.id.tvProductName);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvProductSize = findViewById(R.id.tvProductSize);
        tvProductStock = findViewById(R.id.tvProductStock);
        tvProductDescription = findViewById(R.id.tvProductDescription);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnOrderNow = findViewById(R.id.btnOrderNow);
        btnBack = findViewById(R.id.btnBack);
        rvFeedbackList = findViewById(R.id.rvFeedbackList);

        rvFeedbackList.setLayoutManager(new LinearLayoutManager(this));
        feedbackAdapter = new FeedbackProductAdapter(this);
        rvFeedbackList.setAdapter(feedbackAdapter);

        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void fetchProductDetail(String productId) {
        apiService.getProductById(productId).enqueue(new Callback<Product>() {
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
        if (product == null) return;

        // Set tên sản phẩm
        tvProductName.setText(product.getProductName());

        double originalPrice = product.getPrice();     // Giá gốc
        double discountAmount = product.getDiscount(); // Giảm giá (có thể = 0)
        double finalPrice = product.getTotal();        // Giá sau giảm

        NumberFormat format = NumberFormat.getInstance(new Locale("vi", "VN"));

        TextView textViewOldPrice = findViewById(R.id.textViewOldPrice);
        TextView textViewDiscountPrice = findViewById(R.id.textViewDiscountPrice);

        // Nếu có giảm giá
        if (discountAmount > 0 && finalPrice < originalPrice) {
            // Giá gốc có gạch ngang
            String formattedOriginal = format.format(originalPrice) + "đ";
            SpannableString spannable = new SpannableString(formattedOriginal);
            spannable.setSpan(new StrikethroughSpan(), 0, formattedOriginal.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textViewOldPrice.setVisibility(View.VISIBLE);
            textViewOldPrice.setText(spannable);

            // Giá giảm (số tiền giảm)
            textViewDiscountPrice.setVisibility(View.VISIBLE);
            textViewDiscountPrice.setText("Ưu đãi: " + format.format(discountAmount) + "%");
        } else {
            // Không có giảm giá: ẩn 2 TextView
            textViewOldPrice.setVisibility(View.GONE);
            textViewDiscountPrice.setVisibility(View.GONE);
        }

        // Giá cuối cùng
        tvProductPrice.setText("Giá: " + format.format(finalPrice) + "đ");

        // Size
        tvProductSize.setText("Size: " + product.getSize());

        // Tồn kho
        tvProductStock.setText("Còn lại: " + product.getStockQuantity() + " sản phẩm");

        // Mô tả
        tvProductDescription.setText(product.getDescription());

        // Ảnh sản phẩm
        Glide.with(this)
                .load(product.getImageUrl())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.image_error)
                .into(imgProductDetail);
    }



    private void fetchFeedbackList(String productId) {
        FeedbackApiClient.getClient().create(ApiService.class)
                .getProductFeedback(productId)
                .enqueue(new Callback<List<Feedback>>() {
                    @Override
                    public void onResponse(Call<List<Feedback>> call, Response<List<Feedback>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            feedbackAdapter.setData(response.body());
                        } else {
                            feedbackAdapter.setData(null);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Feedback>> call, Throwable t) {
                        t.printStackTrace();
                        feedbackAdapter.setData(null);
                    }
                });
    }

    private void showAddToCartDialog() {
        if (currentProduct == null) return;
        int stock = currentProduct.getStockQuantity();
        if (stock <= 0) {
            Toast.makeText(this, "Sản phẩm đã hết hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        View view = getLayoutInflater().inflate(R.layout.dialog_order_quantity, null);
        TextView tvQuantity = view.findViewById(R.id.tvQuantity);
        Button btnIncrease = view.findViewById(R.id.btnIncrease);
        Button btnDecrease = view.findViewById(R.id.btnDecrease);
        Button btnConfirm = view.findViewById(R.id.btnConfirmOrder);
        btnConfirm.setText("Thêm vào giỏ hàng");

        final int[] quantity = {1};
        tvQuantity.setText(String.valueOf(quantity[0]));

        btnIncrease.setOnClickListener(v -> {
            if (quantity[0] < stock) {
                quantity[0]++;
                tvQuantity.setText(String.valueOf(quantity[0]));
            }
        });

        btnDecrease.setOnClickListener(v -> {
            if (quantity[0] > 1) {
                quantity[0]--;
                tvQuantity.setText(String.valueOf(quantity[0]));
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();
        btnConfirm.setOnClickListener(v -> {
            CartItem item = new CartItem(
                    currentProduct.getProductID(),
                    currentProduct.getProductName(),
                    currentProduct.getImageUrl(),
                    currentProduct.getDiscount(), // Lấy giá đã giảm
                    quantity[0]
            );
            CartStorage.getInstance().addToCart(item);
            CartStorage.saveCart(getApplicationContext(), CartStorage.getInstance().getCartItems());
            Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
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

        View view = getLayoutInflater().inflate(R.layout.dialog_order_quantity, null);
        TextView tvQuantity = view.findViewById(R.id.tvQuantity);
        Button btnIncrease = view.findViewById(R.id.btnIncrease);
        Button btnDecrease = view.findViewById(R.id.btnDecrease);
        Button btnConfirm = view.findViewById(R.id.btnConfirmOrder);
        btnConfirm.setText("Đặt hàng");

        final int[] quantity = {1};
        tvQuantity.setText(String.valueOf(quantity[0]));

        btnIncrease.setOnClickListener(v -> {
            if (quantity[0] < stock) {
                quantity[0]++;
                tvQuantity.setText(String.valueOf(quantity[0]));
            }
        });

        btnDecrease.setOnClickListener(v -> {
            if (quantity[0] > 1) {
                quantity[0]--;
                tvQuantity.setText(String.valueOf(quantity[0]));
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();
        btnConfirm.setOnClickListener(v -> {
            SessionManager sessionManager = new SessionManager(this);
            String token = sessionManager.getToken();

            if (token == null || token.isEmpty()) {
                Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                return;
            }

            List<ProductOrderDetail> items = new ArrayList<>();
            items.add(new ProductOrderDetail(currentProduct.getProductID(), quantity[0]));

            OrderRequest request = new OrderRequest("", items);
            apiService.createOrder("Bearer " + token, request).enqueue(new Callback<OrderResponse>() {
                @Override
                public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(ProductDetailActivity.this, "Đặt hàng thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProductDetailActivity.this, "Lỗi đặt hàng", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                }

                @Override
                public void onFailure(Call<OrderResponse> call, Throwable t) {
                    Toast.makeText(ProductDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }
}
