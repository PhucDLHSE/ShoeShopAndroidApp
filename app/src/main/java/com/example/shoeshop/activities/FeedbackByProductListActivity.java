package com.example.shoeshop.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
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
import com.example.shoeshop.network.FeedbackApiClient;
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
//    private List<Feedback> list = new ArrayList<>();
    private ProgressBar pb;
    private String currentProductId;

    @Override protected void onCreate(@Nullable Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_feedback_by_product_list);
        currentProductId = getIntent().getStringExtra("productId");
        if (currentProductId == null || currentProductId.isEmpty()) {
            Toast.makeText(this, "Product ID is missing or empty", Toast.LENGTH_SHORT).show();
            Log.e("FEEDBACK_ACTIVITY", "Product ID is missing or empty.");
            finish();
            return;
        }

        swipeRefresh = findViewById(R.id.swipeRefreshFeedbackByProduct);
        pb           = findViewById(R.id.pbFeedbackByProduct);
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
        ApiService api = FeedbackApiClient.getClient().create(ApiService.class);
        api.getProductFeedbacks("Bearer " + new SessionManager(this).getToken(), productId)
                .enqueue(new Callback<List<Feedback>>() {
                    @Override public void onResponse(Call<List<Feedback>> call, Response<List<Feedback>> resp) {
                        swipeRefresh.setRefreshing(false);
                        pb.setVisibility(View.GONE);
                        if (resp.isSuccessful() && resp.body()!=null) {
                            android.util.Log.d("FEEDBACK_DEBUG", "Response: " + resp.body().size());
//                            list.clear();
//                            list.addAll(resp.body());
                            Log.d("FEEDBACK_DEBUG", "Local list size before updateData: " + resp.body().size());
                            adapter.updateData(resp.body());

                        }else {
                            android.util.Log.e("FEEDBACK_DEBUG", "Response error: " + resp.code());
                        }
                    }
                    @Override public void onFailure(Call<List<Feedback>> call, Throwable t) {
                        swipeRefresh.setRefreshing(false);
                        pb.setVisibility(View.GONE);
                        Toast.makeText(FeedbackByProductListActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
