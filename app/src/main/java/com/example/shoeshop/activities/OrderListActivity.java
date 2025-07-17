package com.example.shoeshop.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeshop.R;
import com.example.shoeshop.adapters.OrderAdapter;
import com.example.shoeshop.models.Feedback;
import com.example.shoeshop.models.FeedbackRequest;
import com.example.shoeshop.models.Order;
import com.example.shoeshop.network.ApiClient;
import com.example.shoeshop.network.ApiService;
import com.example.shoeshop.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvHeaderTitle, tvEmpty;
    private ImageView btnBack;

    private OrderAdapter orderAdapter;
    private ApiService apiService;
    private SessionManager sessionManager;
    private String status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);

        /* ------- Ánh xạ View ------- */
        recyclerView   = findViewById(R.id.recyclerViewOrders);
        progressBar    = findViewById(R.id.progressBar);
        tvHeaderTitle  = findViewById(R.id.tvHeaderTitle);
        btnBack        = findViewById(R.id.btnBack);
        tvEmpty        = findViewById(R.id.tvEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        /* ------- Khởi tạo API / Session ------- */
        sessionManager = new SessionManager(this);
        apiService     = ApiClient.getClient().create(ApiService.class);

        /* ------- Lấy trạng thái cần hiển thị ------- */
        status = getIntent().getStringExtra("status");
        if (status == null) status = "ordered";
        tvHeaderTitle.setText(getHeaderTitle(status));

        btnBack.setOnClickListener(v -> finish());

        fetchOrdersByStatus(status);
    }

    /* ============================ API CALL ============================ */

    private void fetchOrdersByStatus(String status) {
        String token = sessionManager.getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            sessionManager.logout();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        Call<List<Order>> call;
        switch (status) {
            case "ordered":       call = apiService.getOrderedOrders      ("Bearer " + token); break;
            case "processing":    call = apiService.getProcessingOrders   ("Bearer " + token); break;
            case "waiting-ship":  call = apiService.getWaitingShipOrders ("Bearer " + token); break;
            case "shipping":      call = apiService.getShippingOrders     ("Bearer " + token); break;
            case "complete":      call = apiService.getCompleteOrders     ("Bearer " + token); break;
            default:
                Toast.makeText(this, "Trạng thái đơn hàng không hợp lệ", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
        }

        call.enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Order> list = response.body();
                    if (list.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setAdapter(null);
                    } else {
                        orderAdapter = new OrderAdapter(
                                OrderListActivity.this,
                                list,
                                status,
                                (productId, productName) -> showReviewDialog(productId, productName)
                        );
                        recyclerView.setAdapter(orderAdapter);
                    }
                } else {
                    Toast.makeText(OrderListActivity.this,
                            "Không thể tải danh sách đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(OrderListActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showReviewDialog(String productId, String productName) {
        View view = getLayoutInflater().inflate(R.layout.dialog_feedback, null);

        TextView tvProductName = view.findViewById(R.id.tvProductName);
        RatingBar ratingBar = view.findViewById(R.id.ratingBar);
        EditText edtComment = view.findViewById(R.id.edtComment);
        Button btnSend = view.findViewById(R.id.btnSend);

        tvProductName.setText(productName);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();

        btnSend.setOnClickListener(v -> {
            int rating = (int) ratingBar.getRating();
            String comment = edtComment.getText().toString().trim();

            if (rating == 0 || comment.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ đánh giá và bình luận", Toast.LENGTH_SHORT).show();
                return;
            }

            sendFeedback(productId, rating, comment);
            dialog.dismiss();
        });

        dialog.show();
    }
    private void sendFeedback(String productId, int rating, String comment) {
        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.getToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        FeedbackRequest request = new FeedbackRequest(productId, rating, comment);
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        apiService.sendFeedback("Bearer " + token, request).enqueue(new Callback<Feedback>() {
            @Override
            public void onResponse(Call<Feedback> call, Response<Feedback> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(OrderListActivity.this, "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(OrderListActivity.this, "Không thể gửi đánh giá", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Feedback> call, Throwable t) {
                Toast.makeText(OrderListActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    /* ============================ UTIL ============================ */

    private String getHeaderTitle(String status) {
        switch (status) {
            case "ordered":      return "Đơn hàng đã đặt";
            case "processing":   return "Đang thực hiện";
            case "waiting-ship": return "Chờ vận chuyển";
            case "shipping":     return "Đang giao hàng";
            case "complete":     return "Đã giao hàng";
            default:             return "Đơn hàng";
        }
    }
}
