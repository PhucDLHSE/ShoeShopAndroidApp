package com.example.shoeshop.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.shoeshop.R;
import com.example.shoeshop.adapters.FeedbackAdapter;
import com.example.shoeshop.models.Feedback;
import com.example.shoeshop.network.ApiClient;
import com.example.shoeshop.network.ApiService;
import com.example.shoeshop.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class FeedbackByProductListActivity extends AppCompatActivity {
    private RecyclerView rv;
    private SwipeRefreshLayout swipeRefresh;
    private FeedbackAdapter adapter;
    private ProgressBar pb;
    private LinearLayout llNoFeedbacks;
    private String currentProductId;

    @Override protected void onCreate(@Nullable Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_feedback_by_product_list);
        currentProductId = getIntent().getStringExtra("productId");
        if (currentProductId == null || currentProductId.isEmpty()) {
            Toast.makeText(this, "Product ID is missing or empty", Toast.LENGTH_SHORT).show();
            Log.e("FEEDBACK_BY_PRODUCT_LIST", "Product ID is missing or empty.");
            finish();
            return;
        }

        swipeRefresh = findViewById(R.id.swipeRefreshFeedbackByProduct);
        pb           = findViewById(R.id.pbFeedbackByProduct);
        llNoFeedbacks = findViewById(R.id.llNoFeedbacks);
        rv = findViewById(R.id.rvFeedbackByProduct);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FeedbackAdapter(this);
        rv.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(() -> loadByProduct(currentProductId));
        loadByProduct(currentProductId);
    }

    private void loadByProduct(String productId) {
        swipeRefresh.setRefreshing(true);
        pb.setVisibility(View.VISIBLE);
        llNoFeedbacks.setVisibility(View.GONE);
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getProductFeedbacks("Bearer " + new SessionManager(this).getToken(), productId)
                .enqueue(new Callback<List<Feedback>>() {
                    @Override public void onResponse(Call<List<Feedback>> call, Response<List<Feedback>> resp) {
                        swipeRefresh.setRefreshing(false);
                        pb.setVisibility(View.GONE);
                        if (resp.isSuccessful() && resp.body() != null && !resp.body().isEmpty()) {
                            adapter.updateData(resp.body());
                        } else if (resp.isSuccessful() && resp.body() != null && resp.body().isEmpty()) {
                            llNoFeedbacks.setVisibility(View.VISIBLE);
                            adapter.updateData(new ArrayList<>()); // Clear existing data if any
                            Toast.makeText(FeedbackByProductListActivity.this, "No feedbacks found for this product.", Toast.LENGTH_SHORT).show();
                        } else {
                            llNoFeedbacks.setVisibility(View.VISIBLE);
                            android.util.Log.e("FEEDBACK_BY_PRODUCT_LIST", "Response error: " + resp.code());
                            Toast.makeText(FeedbackByProductListActivity.this, "Response error: " + resp.code(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onFailure(Call<List<Feedback>> call, Throwable t) {
                        swipeRefresh.setRefreshing(false);
                        pb.setVisibility(View.GONE);
                        Toast.makeText(FeedbackByProductListActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                        llNoFeedbacks.setVisibility(View.VISIBLE);
                        Log.e("FEEDBACK_BY_PRODUCT_LIST", "Network error: ", t);
                    }
                });
    }
}
