package com.example.shoeshop.activities;

import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.shoeshop.R;
import com.example.shoeshop.models.AddProductResponse;
import com.example.shoeshop.models.Product;
import com.example.shoeshop.models.PutProductRequest;
import com.example.shoeshop.network.ApiService;
import com.example.shoeshop.network.ApiClient;
import com.example.shoeshop.utils.SessionManager;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProductActivity extends AppCompatActivity {
    private EditText etName, etDesc, etImageUrl, etSize, etColor, etPrice, etDiscount, etStock;
    private Button btnUpdate, btnCancel;
    private Uri selectedImageUri;
    private static final int PICK_IMAGE_REQUEST = 100;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);
        String productId = getIntent().getStringExtra("productId");

        etName     = findViewById(R.id.etProductName);
        etDesc     = findViewById(R.id.etDescription);
        etImageUrl = findViewById(R.id.etImageUrl);
        etSize     = findViewById(R.id.etSize);
        etColor    = findViewById(R.id.etColor);
        etPrice    = findViewById(R.id.etPrice);
        etDiscount = findViewById(R.id.etDiscount);
        etStock    = findViewById(R.id.etStockQuantity);
        btnUpdate  = findViewById(R.id.btnUpdateProduct);
        btnCancel  = findViewById(R.id.btnCancelProduct);

        ApiService api = ApiClient.getClient().create(ApiService.class);
        String token = new SessionManager(this).getToken();

        // Load product by id
        api.getProductById(productId).enqueue(new Callback<Product>(){
            @Override public void onResponse(Call<Product> c, Response<Product> r) {
                if(r.isSuccessful()&&r.body()!=null){
                    Product p = r.body();
                    etName.setText(p.getProductName());
                    etDesc.setText(p.getDescription());
                    etImageUrl.setText(p.getImageUrl());
                    etSize.setText(p.getSize());
                    etColor.setText(p.getColor());
                    etPrice.setText(String.valueOf(p.getPrice()));
                    etDiscount.setText(String.valueOf(p.getDiscount()));
                    etStock.setText(String.valueOf(p.getStockQuantity()));
                }
            }
            @Override public void onFailure(Call<Product> c, Throwable t){}
        });

        btnCancel.setOnClickListener(v -> finish());

        btnUpdate.setOnClickListener(v -> {
            MediaType text = MediaType.parse("text/plain");

            RequestBody rbName     = RequestBody.create(text, etName.getText().toString());
            RequestBody rbDesc     = RequestBody.create(text, etDesc.getText().toString());
            RequestBody rbUrl      = RequestBody.create(text, etImageUrl.getText().toString());
            RequestBody rbSize     = RequestBody.create(text, etSize.getText().toString());
            RequestBody rbColor    = RequestBody.create(text, etColor.getText().toString());
            RequestBody rbPrice    = RequestBody.create(text, etPrice.getText().toString());
            RequestBody rbDiscount = RequestBody.create(text, etDiscount.getText().toString());
            RequestBody rbStock    = RequestBody.create(text, etStock.getText().toString());

            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("ImageFile", "", RequestBody.create(text, ""));
            // Nếu muốn hỗ trợ chọn ảnh thật thì thêm code pick ảnh + File utils

            api.updateProduct("Bearer " + token, productId,
                    rbName, rbDesc, imagePart, rbUrl, rbSize, rbColor, rbPrice, rbDiscount, rbStock
            ).enqueue(new Callback<AddProductResponse>() {
                @Override
                public void onResponse(Call<AddProductResponse> call, Response<AddProductResponse> response) {
                    if (response.isSuccessful()) finish();
                    else Toast.makeText(EditProductActivity.this, "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onFailure(Call<AddProductResponse> call, Throwable t) {
                    Toast.makeText(EditProductActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
