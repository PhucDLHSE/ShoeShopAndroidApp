package com.example.shoeshop.activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.shoeshop.R;
import com.example.shoeshop.models.AddProductResponse;
import com.example.shoeshop.network.ApiService;
import com.example.shoeshop.network.ApiClient;
import com.example.shoeshop.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddNewProductActivity extends AppCompatActivity {
    private TextInputEditText etName, etDesc, etSize, etColor, etDiscount, etSold, etStock, etPrice;
    private Button btnSave, btnCancel;
    private ImageButton ibUploadImage, btnPriceDecrement, btnPriceIncrement,
            btnDiscountDecrement, btnDiscountIncrement, btnSoldQuantityDecrement,
            btnSoldQuantityIncrement, btnStockQuantityDecrement, btnStockQuantityIncrement;
    private ImageView ivProductImagePreview;
    private TextView tvTitle;
    private Uri selectedImageUri;
    private static final int PICK_IMAGE_REQUEST = 200;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        // Bind views
        tvTitle = findViewById(R.id.tvAddProductTitle);
        etName = findViewById(R.id.etProductName);
        etDesc = findViewById(R.id.etDescription);
        etSize = findViewById(R.id.etSize);
        etColor = findViewById(R.id.etColor);
        etPrice = findViewById(R.id.etPrice);
        etDiscount = findViewById(R.id.etDiscount);
        etSold = findViewById(R.id.etSoldQuantity);
        etStock = findViewById(R.id.etStockQuantity);
        btnSave = findViewById(R.id.btnSaveProduct);
        btnCancel = findViewById(R.id.btnCancelProduct);
        ibUploadImage = findViewById(R.id.ibUploadImage);
        ivProductImagePreview = findViewById(R.id.ivProductImagePreview);
        btnPriceDecrement = findViewById(R.id.btnPriceDecrement);
        btnPriceIncrement = findViewById(R.id.btnPriceIncrement);
        btnDiscountDecrement = findViewById(R.id.btnDiscountDecrement);
        btnDiscountIncrement = findViewById(R.id.btnDiscountIncrement);
        btnSoldQuantityDecrement = findViewById(R.id.btnSoldQuantityDecrement);
        btnSoldQuantityIncrement = findViewById(R.id.btnSoldQuantityIncrement);
        btnStockQuantityDecrement = findViewById(R.id.btnStockQuantityDecrement);
        btnStockQuantityIncrement = findViewById(R.id.btnStockQuantityIncrement);

        // Set Vietnamese text
        tvTitle.setText("Thêm Sản Phẩm Mới");
        etPrice.setHint("0");
        etDiscount.setHint("0");
        etSold.setHint("0");
        etStock.setHint("0");
        btnSave.setText("Lưu");
        btnCancel.setText("Hủy");

        // Pick image
        ibUploadImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        // Price increment/decrement
        btnPriceIncrement.setOnClickListener(v -> {
            int curr = parseIntSafe(etPrice);
            setIntSafe(etPrice, curr + 1);
        });
        btnPriceDecrement.setOnClickListener(v -> {
            int curr = parseIntSafe(etPrice);
            setIntSafe(etPrice, curr - 1);
        });

        // Discount increment/decrement
        btnDiscountIncrement.setOnClickListener(v -> {
            int curr = parseIntSafe(etDiscount);
            setIntSafe(etDiscount, curr + 1);
        });
        btnDiscountDecrement.setOnClickListener(v -> {
            int curr = parseIntSafe(etDiscount);
            setIntSafe(etDiscount, curr - 1);
        });

        // Sold quantity increment/decrement
        btnSoldQuantityIncrement.setOnClickListener(v -> {
            int curr = parseIntSafe(etSold);
            setIntSafe(etSold, curr + 1);
        });
        btnSoldQuantityDecrement.setOnClickListener(v -> {
            int curr = parseIntSafe(etSold);
            setIntSafe(etSold, curr - 1);
        });

        // Stock quantity increment/decrement
        btnStockQuantityIncrement.setOnClickListener(v -> {
            int curr = parseIntSafe(etStock);
            setIntSafe(etStock, curr + 1);
        });
        btnStockQuantityDecrement.setOnClickListener(v -> {
            int curr = parseIntSafe(etStock);
            setIntSafe(etStock, curr - 1);
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
            ivProductImagePreview.setImageURI(selectedImageUri);
        }
    }

    private File getFileFromUri(Uri uri) {
        File file = null;
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            String fileName = getFileNameFromUri(uri);
            File tempFile = new File(getCacheDir(), fileName);
            try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            }
            file = tempFile;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    private void uploadProduct() {
        // Validate inputs
        String name = etName.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();
        String size = etSize.getText().toString().trim();
        String color = etColor.getText().toString().trim();
        String price = etPrice.getText().toString().trim();
        String discount = etDiscount.getText().toString().trim();
        String soldQty = etSold.getText().toString().trim();
        String stockQty = etStock.getText().toString().trim();

        if (name.isEmpty() || price.isEmpty() || stockQty.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ tên, giá và số lượng tồn", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedImageUri == null) {
            Toast.makeText(this, "Vui lòng chọn ảnh sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService api = ApiClient.getClient().create(ApiService.class);
        String token = new SessionManager(this).getToken();
        MediaType textType = MediaType.parse("text/plain");

        // Text parts
        RequestBody rbName = RequestBody.create(textType, name);
        RequestBody rbDesc = RequestBody.create(textType, desc);
        RequestBody rbImageUrl = RequestBody.create(textType, "");
        RequestBody rbSize = RequestBody.create(textType, size);
        RequestBody rbColor = RequestBody.create(textType, color);
        RequestBody rbPrice = RequestBody.create(textType, price);
        RequestBody rbDiscount = RequestBody.create(textType, discount.isEmpty() ? "0" : discount);
        RequestBody rbSold = RequestBody.create(textType, soldQty.isEmpty() ? "0" : soldQty);
        RequestBody rbStock = RequestBody.create(textType, stockQty);

        // Image part
        File imageFile = getFileFromUri(selectedImageUri);
        RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), imageFile);
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("ImageFile", imageFile.getName(), fileBody);

        // API call
        api.addProduct(
                "Bearer " + token,
                rbName, rbDesc,
                imagePart, rbImageUrl,
                rbSize, rbColor,
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
                Toast.makeText(AddNewProductActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper để parse int an toàn
    private int parseIntSafe(TextInputEditText et) {
        String s = et.getText() != null ? et.getText().toString().trim() : "";
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }
    // Helper để set lại giá trị vào EditText
    private void setIntSafe(TextInputEditText et, int value) {
        et.setText(String.valueOf(Math.max(0, value)));
    }

}
