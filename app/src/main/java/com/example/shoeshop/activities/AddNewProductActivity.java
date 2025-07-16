package com.example.shoeshop.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.shoeshop.R;
import com.example.shoeshop.models.AddProductRequest;
import com.example.shoeshop.models.AddProductResponse;
import com.example.shoeshop.network.ApiService;
import com.example.shoeshop.network.ApiClient;
import com.example.shoeshop.utils.SessionManager;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddNewProductActivity extends AppCompatActivity {
    private EditText etName, etDesc, etImageUrl, etSize, etColor, etPrice, etDiscount, etStock, etSold;
    private Button btnSave, btnCancel, btnPickImage;
    private Uri selectedImageUri;
    private TextView tvTitle;
    private static final int PICK_IMAGE_REQUEST = 200;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        // Bind views
        tvTitle      = findViewById(R.id.tvAddProductTitle);
        etName       = findViewById(R.id.etProductName);
        etDesc       = findViewById(R.id.etDescription);
        etImageUrl   = findViewById(R.id.etImageUrl);
        etSize       = findViewById(R.id.etSize);
        etColor      = findViewById(R.id.etColor);
        etPrice      = findViewById(R.id.etPrice);
        etDiscount   = findViewById(R.id.etDiscount);
        etSold       = findViewById(R.id.etSoldQuantity);
        etStock      = findViewById(R.id.etStockQuantity);
        btnSave      = findViewById(R.id.btnSaveProduct);
        btnCancel    = findViewById(R.id.btnCancelProduct);
        btnPickImage = findViewById(R.id.btnPickImage);

        //set all text vietnamese here

        tvTitle.setText("Thêm Sản Phẩm Mới");
        etName.setHint("Tên sản phẩm");
        etDesc.setHint("Mô tả");
        etImageUrl.setHint("URL hình ảnh");
        etSize.setHint("Kích cỡ");
        etColor.setHint("Màu sắc");
        etPrice.setHint("Giá");
        etDiscount.setHint("Giảm giá (%)");
        etSold.setHint("Đã bán");
        etStock.setHint("Tồn kho");
        btnPickImage.setText("Chọn ảnh");
        btnSave.setText("Lưu");
        btnCancel.setText("Hủy");

        // Pick image
        btnPickImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        // Cancel
        btnCancel.setOnClickListener(v -> finish());

        // Save
        btnSave.setOnClickListener(v -> uploadProduct());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            etImageUrl.setText(selectedImageUri.toString());
        }
    }

    private void uploadProduct() {
        // Validate inputs
        String name     = etName.getText().toString().trim();
        String desc     = etDesc.getText().toString().trim();
        String imageUrl = etImageUrl.getText().toString().trim();
        String size     = etSize.getText().toString().trim();
        String color    = etColor.getText().toString().trim();
        String price    = etPrice.getText().toString().trim();
        String discount = etDiscount.getText().toString().trim();
        String soldQty  = etSold.getText().toString().trim();
        String stockQty = etStock.getText().toString().trim();
        if (name.isEmpty() || price.isEmpty() || stockQty.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ tên, giá và số lượng tồn", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService api = ApiClient.getClient().create(ApiService.class);
        String token = new SessionManager(this).getToken();
        MediaType text = MediaType.parse("text/plain");

        // Prepare RequestBody
        RequestBody rbName     = RequestBody.create(text, name);
        RequestBody rbDesc     = RequestBody.create(text, desc);
        RequestBody rbUrl      = RequestBody.create(text, imageUrl);
        RequestBody rbSize     = RequestBody.create(text, size);
        RequestBody rbColor    = RequestBody.create(text, color);
        RequestBody rbPrice    = RequestBody.create(text, price);
        RequestBody rbDiscount = RequestBody.create(text, discount.isEmpty() ? "0" : discount);
        RequestBody rbSold     = RequestBody.create(text, soldQty.isEmpty() ? "0" : soldQty);
        RequestBody rbStock    = RequestBody.create(text, stockQty);

        // Multipart for image file
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("ImageFile", "", RequestBody.create(text, ""));

        // Call API
        api.addProduct(
                "Bearer " + token,
                rbName, rbDesc,
                imagePart,
                rbUrl, rbSize, rbColor,
                rbPrice, rbDiscount,
                rbSold, rbStock
        ).enqueue(new Callback<AddProductResponse>() {
            @Override
            public void onResponse(Call<AddProductResponse> call, Response<AddProductResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddNewProductActivity.this, "Thêm thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddNewProductActivity.this, "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<AddProductResponse> call, Throwable t) {
                Toast.makeText(AddNewProductActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}