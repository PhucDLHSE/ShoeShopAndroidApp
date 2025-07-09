package com.example.shoeshop.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeshop.R;
import com.example.shoeshop.adapters.AddressAdapter;
import com.example.shoeshop.models.CustomerAddress;
import com.example.shoeshop.network.ApiClient;
import com.example.shoeshop.network.ApiService;
import com.example.shoeshop.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddressActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Button btnAddAddress;
    private AddressAdapter adapter;
    private ApiService apiService;
    private SessionManager sessionManager;

    private static final String TAG = "AddressActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        recyclerView = findViewById(R.id.recyclerViewAddresses);
        btnAddAddress = findViewById(R.id.btnAddAddress);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        sessionManager = new SessionManager(this);
        apiService = ApiClient.getClient().create(ApiService.class);

        // Bắt sự kiện nút back trong header mới
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadAddresses();

        btnAddAddress.setOnClickListener(v -> showAddAddressDialog());
    }

    private void loadAddresses() {
        String token = sessionManager.getToken();
        if (token == null) {
            Toast.makeText(this, "Token không tồn tại, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            sessionManager.logout();
            return;
        }

        apiService.getMyAddresses("Bearer " + token)
                .enqueue(new Callback<List<CustomerAddress>>() {
                    @Override
                    public void onResponse(Call<List<CustomerAddress>> call, Response<List<CustomerAddress>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            adapter = new AddressAdapter(response.body(), addressId -> {
                                setDefaultAddress(addressId);
                            });
                            recyclerView.setAdapter(adapter);
                        } else {
                            Toast.makeText(AddressActivity.this, "Không thể tải địa chỉ (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<CustomerAddress>> call, Throwable t) {
                        Toast.makeText(AddressActivity.this, "Lỗi kết nối: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showAddAddressDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_address, null);

        EditText edtFullAddress = dialogView.findViewById(R.id.edtFullAddress);
        EditText edtStreet = dialogView.findViewById(R.id.edtStreet);
        EditText edtWard = dialogView.findViewById(R.id.edtWard);
        EditText edtDistrict = dialogView.findViewById(R.id.edtDistrict);
        EditText edtCity = dialogView.findViewById(R.id.edtCity);
        CheckBox chkIsDefault = dialogView.findViewById(R.id.chkIsDefault);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Thêm địa chỉ mới")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String fullAddress = edtFullAddress.getText().toString().trim();
                    String street = edtStreet.getText().toString().trim();
                    String ward = edtWard.getText().toString().trim();
                    String district = edtDistrict.getText().toString().trim();
                    String city = edtCity.getText().toString().trim();
                    boolean isDefault = chkIsDefault.isChecked();

                    if (fullAddress.isEmpty() || street.isEmpty() || ward.isEmpty() || district.isEmpty() || city.isEmpty()) {
                        Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    addAddress(fullAddress, street, ward, district, city, isDefault);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void addAddress(String fullAddress, String street, String ward, String district, String city, boolean isDefault) {
        String token = sessionManager.getToken();

        CustomerAddress address = new CustomerAddress();
        address.setFullAddress(fullAddress);
        address.setStreetAddress(street);
        address.setWard(ward);
        address.setDistrict(district);
        address.setCity(city);
        address.setDefault(isDefault);
        address.setContactName(sessionManager.getUserName());
        address.setContactPhone(sessionManager.getUserPhone());

        Call<CustomerAddress> call = apiService.addCustomerAddress("Bearer " + token, address);
        call.enqueue(new Callback<CustomerAddress>() {
            @Override
            public void onResponse(Call<CustomerAddress> call, Response<CustomerAddress> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddressActivity.this, "Đã thêm địa chỉ", Toast.LENGTH_SHORT).show();
                    loadAddresses();
                } else {
                    Toast.makeText(AddressActivity.this, "Thêm địa chỉ thất bại", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "POST address error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CustomerAddress> call, Throwable t) {
                Toast.makeText(AddressActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setDefaultAddress(String addressId) {
        String token = sessionManager.getToken();

        Call<Void> call = apiService.setDefaultAddress("Bearer " + token, addressId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddressActivity.this, "Đã đặt địa chỉ mặc định", Toast.LENGTH_SHORT).show();
                    loadAddresses();
                } else {
                    Toast.makeText(AddressActivity.this, "Không thể đặt địa chỉ mặc định", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "PATCH set-default error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AddressActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "setDefaultAddress error", t);
            }
        });
    }
}
