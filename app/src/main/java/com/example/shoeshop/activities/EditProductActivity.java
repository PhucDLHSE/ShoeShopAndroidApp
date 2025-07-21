package com.example.shoeshop.activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import androidx.appcompat.app.AppCompatActivity;
import com.example.shoeshop.R;
import com.example.shoeshop.models.AddProductResponse;
import com.example.shoeshop.models.Product;
import com.example.shoeshop.network.ApiService;
import com.example.shoeshop.network.ApiClient;
import com.example.shoeshop.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProductActivity extends AppCompatActivity {
    private TextInputEditText etName, etDesc, etSize, etColor, etPrice, etDiscount, etStock;
    private Button btnUpdate, btnCancel;
    private Uri selectedImageUri;
    private ImageView ivProductImagePreview;
    private ImageButton ibUploadImage, btnPriceDecrement, btnPriceIncrement,
            btnDiscountDecrement, btnDiscountIncrement, btnStockQuantityDecrement, btnStockQuantityIncrement;
    private static final int PICK_IMAGE_REQUEST = 100;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);
        String productId = getIntent().getStringExtra("productId");

        etName     = findViewById(R.id.etProductName);
        etDesc     = findViewById(R.id.etDescription);
        ivProductImagePreview = findViewById(R.id.ivProductImagePreview);
        etSize     = findViewById(R.id.etSize);
        etColor    = findViewById(R.id.etColor);
        etPrice    = findViewById(R.id.etPrice);
        etDiscount = findViewById(R.id.etDiscount);
        etStock    = findViewById(R.id.etStockQuantity);
        ibUploadImage = findViewById(R.id.ibUploadImage);
        btnUpdate  = findViewById(R.id.btnUpdateProduct);
        btnCancel  = findViewById(R.id.btnCancelProduct);
        btnPriceDecrement = findViewById(R.id.btnPriceDecrement);
        btnPriceIncrement = findViewById(R.id.btnPriceIncrement);
        btnDiscountDecrement = findViewById(R.id.btnDiscountDecrement);
        btnDiscountIncrement = findViewById(R.id.btnDiscountIncrement);
        btnStockQuantityDecrement = findViewById(R.id.btnStockQuantityDecrement);
        btnStockQuantityIncrement = findViewById(R.id.btnStockQuantityIncrement);

        ApiService api = ApiClient.getClient().create(ApiService.class);
        String token = new SessionManager(this).getToken();

        ibUploadImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        // Load product by id
        api.getProductById(productId).enqueue(new Callback<Product>(){
            @Override public void onResponse(Call<Product> c, Response<Product> r) {
                if(r.isSuccessful()&&r.body()!=null){
                    Product p = r.body();
                    etName.setText(p.getProductName());
                    etDesc.setText(p.getDescription());
                    Glide.with(EditProductActivity.this)
                            .load(p.getImageUrl())
                            .placeholder(R.drawable.ic_image)
                            .into(ivProductImagePreview);
                    etSize.setText(p.getSize());
                    etColor.setText(p.getColor());
                    etPrice.setText(String.valueOf(p.getPrice()));
                    etDiscount.setText(String.valueOf(p.getDiscount()));
                    etStock.setText(String.valueOf(p.getStockQuantity()));
                    setupIncrementDecrementButtons();
                }
            }
            @Override public void onFailure(Call<Product> c, Throwable t){}
        });

        btnCancel.setOnClickListener(v -> finish());

        btnUpdate.setOnClickListener(v -> {
            MediaType text = MediaType.parse("text/plain");

            RequestBody rbName     = RequestBody.create(text, etName.getText().toString());
            RequestBody rbDesc     = RequestBody.create(text, etDesc.getText().toString());
            RequestBody rbSize     = RequestBody.create(text, etSize.getText().toString());
            RequestBody rbColor    = RequestBody.create(text, etColor.getText().toString());
            RequestBody rbPrice    = RequestBody.create(text, etPrice.getText().toString());
            RequestBody rbDiscount = RequestBody.create(text, etDiscount.getText().toString());
            RequestBody rbStock    = RequestBody.create(text, etStock.getText().toString());

            MultipartBody.Part imagePart;
            if (selectedImageUri != null) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                    byte[] imageBytes = getBytes(inputStream);

                    RequestBody requestFile = RequestBody.create(
                            MediaType.parse(getContentResolver().getType(selectedImageUri)),
                            imageBytes
                    );
                    imagePart = MultipartBody.Part.createFormData(
                            "ImageFile",
                            "uploaded_image.jpg",
                            requestFile
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Không thể đọc ảnh đã chọn", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else { // If not update image, keep the old one by sending null
                imagePart = MultipartBody.Part.createFormData("ImageFile", "", RequestBody.create(MediaType.parse("image/*"), ""));
            }

            // rbUrl is removed as we are uploading the image file directly
            RequestBody rbUrl = RequestBody.create(text, ""); // Send empty or handle accordingly on the backend

            api.updateProduct("Bearer " + token, productId,
                    rbName, rbDesc, imagePart, rbUrl, rbSize, rbColor, rbPrice, rbDiscount, rbStock
            ).enqueue(new Callback<AddProductResponse>() {
                @Override
                public void onResponse(@Nullable Call<AddProductResponse> call, @Nullable Response<AddProductResponse> response) {
                    if (response.isSuccessful()) finish();
                    else Toast.makeText(EditProductActivity.this, "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onFailure(Call<AddProductResponse> call, Throwable t) {
                    Toast.makeText(EditProductActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    Log.e("EditProductActivity", "Network error", t);
                }
            });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            Glide.with(this).load(selectedImageUri).into(ivProductImagePreview);
        }
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { android.provider.MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }
    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

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
    private void setupIncrementDecrementButtons() {
        btnPriceIncrement.setOnClickListener(v -> {
            int curr = parseIntSafe(etPrice);
            setIntSafe(etPrice, curr + 1);
        });
        btnPriceDecrement.setOnClickListener(v -> {
            int curr = parseIntSafe(etPrice);
            setIntSafe(etPrice, curr - 1);
        });

        btnDiscountIncrement.setOnClickListener(v -> {
            int curr = parseIntSafe(etDiscount);
            setIntSafe(etDiscount, curr + 1);
        });
        btnDiscountDecrement.setOnClickListener(v -> {
            int curr = parseIntSafe(etDiscount);
            setIntSafe(etDiscount, curr - 1);
        });

        btnStockQuantityIncrement.setOnClickListener(v -> {
            int curr = parseIntSafe(etStock);
            setIntSafe(etStock, curr + 1);
        });
        btnStockQuantityDecrement.setOnClickListener(v -> {
            int curr = parseIntSafe(etStock);
            setIntSafe(etStock, curr - 1);
        });
    }

}