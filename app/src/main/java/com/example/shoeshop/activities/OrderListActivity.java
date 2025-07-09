package com.example.shoeshop.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeshop.R;
import com.example.shoeshop.adapters.OrderAdapter;
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
    private TextView tvHeaderTitle;
    private ImageView btnBack;

    private OrderAdapter orderAdapter;
    private ApiService apiService;
    private SessionManager sessionManager;
    private String status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);

        recyclerView = findViewById(R.id.recyclerViewOrders);
        progressBar = findViewById(R.id.progressBar);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        btnBack = findViewById(R.id.btnBack);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        sessionManager = new SessionManager(this);
        apiService = ApiClient.getClient().create(ApiService.class);

        status = getIntent().getStringExtra("status");
        if (status == null) status = "ordered";

        switch (status) {
            case "ordered":
                tvHeaderTitle.setText("Đơn hàng đã đặt");
                break;
            case "processing":
                tvHeaderTitle.setText("Đang thực hiện");
                break;
            case "waiting-ship":
                tvHeaderTitle.setText("Chờ vận chuyển");
                break;
            case "shipping":
                tvHeaderTitle.setText("Đang giao hàng");
                break;
            case "complete":
                tvHeaderTitle.setText("Đã giao hàng");
                break;
            default:
                tvHeaderTitle.setText("Đơn hàng");
        }

        btnBack.setOnClickListener(v -> finish());

        fetchOrdersByStatus(status);
    }

    private void fetchOrdersByStatus(String status) {
        String token = sessionManager.getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            sessionManager.logout();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        Call<List<Order>> call;
        if (status.equals("ordered")) {
            call = apiService.getOrderedOrders("Bearer " + token);
        } else if (status.equals("processing")) {
            call = apiService.getProcessingOrders("Bearer " + token);
        } else if (status.equals("waiting-ship")) {
            call = apiService.getWaitingShipOrders("Bearer " + token);
        } else if(status.equals("shipping")){
            call = apiService.getShippingOrders("Bearer " + token);
        } else if(status.equals("complete")) {
            call = apiService.getCompleteOrders("Bearer " + token);
        } else  {
            Toast.makeText(this, "Trạng thái đơn hàng không hợp lệ", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }


        call.enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    orderAdapter = new OrderAdapter(response.body());
                    recyclerView.setAdapter(orderAdapter);
                } else {
                    Toast.makeText(OrderListActivity.this, "Không thể tải danh sách đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(OrderListActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
