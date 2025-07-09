package com.example.shoeshop.activities;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.shoeshop.R;

public class PaymentActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_payment);
        String url = getIntent().getStringExtra("qrUrl");
        ImageView iv = findViewById(R.id.ivQr);
        Glide.with(this).load(url).into(iv);
    }
}