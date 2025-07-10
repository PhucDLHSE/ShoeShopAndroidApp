package com.example.shoeshop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shoeshop.R;
import com.example.shoeshop.models.CreateDeliveryRequest;
import com.example.shoeshop.models.PatchDeliveryResponse;
import com.example.shoeshop.network.ApiClient;
import com.example.shoeshop.network.ApiService;
import com.example.shoeshop.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateDeliveryNoteActivity extends AppCompatActivity {
    private Button btnChooseOrder, btnCreate, btnCancel;
    private TextView tvChosenOrder;
    private String chosenOrderId = null;

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_create_delivery);

        btnChooseOrder = findViewById(R.id.btnChooseOrder);
        btnCreate      = findViewById(R.id.btnCreate);
        btnCancel      = findViewById(R.id.btnCancel);

        btnChooseOrder.setOnClickListener(v -> {
            startActivityForResult(
                    new Intent(this, ChooseDeliveryOrderActivity.class),
                    100
            );
        });

        btnCancel.setOnClickListener(v -> finish());

        btnCreate.setOnClickListener(v -> {
            if (chosenOrderId == null) {
                Toast.makeText(this, "Chưa chọn đơn hàng", Toast.LENGTH_SHORT).show();
                return;
            }
            // Lấy token, tên, phone, note
            String token = new SessionManager(this).getToken();
            String shipper = ((EditText)findViewById(R.id.etShipperName)).getText().toString();
            String phone   = ((EditText)findViewById(R.id.etShipperPhone)).getText().toString();
            String note    = ((EditText)findViewById(R.id.etNote)).getText().toString();
            CreateDeliveryRequest req = new CreateDeliveryRequest(chosenOrderId, shipper, phone, note);
            ApiService apiService = ApiClient.getClient().create(ApiService.class);
            apiService.createDelivery("Bearer "+token, req).enqueue(new Callback<PatchDeliveryResponse>() {
                @Override public void onResponse(Call<PatchDeliveryResponse> call, Response<PatchDeliveryResponse> response) {
                    finish(); // quay về StaffDeliveryFragment, tab “Chờ lấy hàng”
                }
                @Override public void onFailure(Call<PatchDeliveryResponse> call, Throwable t) { /* báo lỗi */ }
            });
        });
    }

    @Override protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req==100 && res==RESULT_OK) {
            chosenOrderId = data.getStringExtra("orderId");
            String display = data.getStringExtra("orderSummary");
            btnChooseOrder.setText(display);
        }
    }
}
