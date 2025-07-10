package com.example.shoeshop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeshop.R;
import com.example.shoeshop.adapters.ChooseOrderAdapter;
import com.example.shoeshop.models.Order;
import com.example.shoeshop.network.ApiClient;
import com.example.shoeshop.network.ApiService;
import com.example.shoeshop.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChooseDeliveryOrderActivity extends AppCompatActivity {
    private RecyclerView rv;
    private String selectedOrderId = null;
    private Button btnSave, btnCancel;

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_choose_delivery_order);
        rv = findViewById(R.id.rvChooseOrders);
        btnSave   = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        rv.setLayoutManager(new LinearLayoutManager(ChooseDeliveryOrderActivity.this));

// Gọi API
        String token = new SessionManager(this).getToken();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getWaitingShipOrders("Bearer " + token)
                .enqueue(new Callback<List<Order>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Order>> call,
                                           @NonNull Response<List<Order>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Order> list = response.body();
                            // Thiết lập adapter trong onResponse, khi đã có dữ liệu
                            ChooseOrderAdapter adapter = new ChooseOrderAdapter(list, id -> {
                                selectedOrderId = id;
                            });
                            rv.setAdapter(adapter);
                        } else {
                            Toast.makeText(ChooseDeliveryOrderActivity.this,
                                    "Lấy danh sách đơn thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Order>> call,
                                          @NonNull Throwable t) {
                        Toast.makeText(ChooseDeliveryOrderActivity.this,
                                "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
        btnSave.setOnClickListener(v -> {
            if (selectedOrderId==null) {
                Toast.makeText(this,"Chưa chọn đơn", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent data = new Intent();
            data.putExtra("orderId", selectedOrderId);
            data.putExtra("orderSummary", "Đơn #"+selectedOrderId);
            setResult(RESULT_OK, data);
            finish();
        });
    }
}
