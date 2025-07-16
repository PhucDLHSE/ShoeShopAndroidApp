package com.example.shoeshop.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.shoeshop.R;
import com.example.shoeshop.adapters.SearchProductAdapter;
import com.example.shoeshop.adapters.StaffProductAdapter;
import com.example.shoeshop.models.Product;
import com.example.shoeshop.network.ApiService;
import com.example.shoeshop.network.ApiClient;
import com.example.shoeshop.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class SearchProductActivity extends AppCompatActivity {
    private RecyclerView rv;
    private SearchProductAdapter adapter;
    private EditText etSearchName, etSearchSize, etSearchColor, etMinPrice, etMaxPrice;
    private Button btnPerformSearch;

    @Override protected void onCreate(@Nullable Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_search_product);

        etSearchName  = findViewById(R.id.etSearchName);
        etSearchSize  = findViewById(R.id.etSearchSize);
        etSearchColor = findViewById(R.id.etSearchColor);
        etMinPrice    = findViewById(R.id.etMinPrice);
        etMaxPrice    = findViewById(R.id.etMaxPrice);
        btnPerformSearch = findViewById(R.id.btnPerformSearch);

        etSearchName.setHint("Tên SP");
        etSearchSize.setHint("Kích Cỡ SP");
        etSearchColor.setHint("Màu SP");
        etMinPrice.setHint("Giá Thấp Nhất");
        etMaxPrice.setHint("Giá Cao Nhất");
        btnPerformSearch.setText("Tìm");

        rv = findViewById(R.id.rvSearchResults);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchProductAdapter(this, new SessionManager(this).getToken());
        rv.setAdapter(adapter);

        btnPerformSearch.setOnClickListener(v -> {
            String name = etSearchName.getText().toString().trim();
            String size = etSearchSize.getText().toString().trim();
            String color = etSearchColor.getText().toString().trim();
            Double min = etMinPrice.getText().toString().isEmpty() ? null : Double.parseDouble(etMinPrice.getText().toString());
            Double max = etMaxPrice.getText().toString().isEmpty() ? null : Double.parseDouble(etMaxPrice.getText().toString());
            performSearch(name, size, color, min, max);
        });

        // initial load empty or all
        performSearch(null, null, null, null, null);
    }

    private void performSearch(String name, String size, String color, Double min, Double max) {
        ApiService api = ApiClient.getClient().create(ApiService.class);
        String token = new SessionManager(this).getToken();
        api.searchProducts(name, size, color, min, max)
                .enqueue(new Callback<List<Product>>() {
                    @Override public void onResponse(Call<List<Product>> c, Response<List<Product>> r) {
                        if (r.isSuccessful() && r.body() != null) adapter.setData(r.body());
                    }
                    @Override public void onFailure(Call<List<Product>> c, Throwable t) {}
                });
    }
}