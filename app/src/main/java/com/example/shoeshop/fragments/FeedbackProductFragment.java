package com.example.shoeshop.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.shoeshop.R;
import com.example.shoeshop.activities.FeedbackByProductListActivity;
import com.example.shoeshop.adapters.ProductSimpleAdapter;
import com.example.shoeshop.models.Product;
import com.example.shoeshop.network.ApiClient;
import com.example.shoeshop.network.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.*;

public class FeedbackProductFragment extends Fragment {
    private RecyclerView rv;
    private ProductSimpleAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar pb;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle b) {
        return i.inflate(R.layout.fragment_feedback_product, c, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        swipeRefresh = v.findViewById(R.id.swipeRefreshProductList);
        pb           = v.findViewById(R.id.pbProductList);
        rv = v.findViewById(R.id.rvProductsForFeedback);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ProductSimpleAdapter(getContext(), list -> {
            Intent intent = new Intent(getContext(), FeedbackByProductListActivity.class);
            intent.putExtra("productId", list.getProductID());
            Log.d("FeedbackProductFragment", "Product ID: " + list.getProductID());
            startActivity(intent);
        });
        rv.setAdapter(adapter);
        // Pull‑to‑refresh → reload products
        swipeRefresh.setOnRefreshListener(this::loadProducts);
        loadProducts();
    }

    private void loadProducts() {
        swipeRefresh.setRefreshing(true);
        pb.setVisibility(View.VISIBLE);
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getAllProducts().enqueue(new Callback<List<Product>>() {
            @Override public void onResponse(Call<List<Product>> c, Response<List<Product>> r) {
                swipeRefresh.setRefreshing(false);
                pb.setVisibility(View.GONE);
                if (r.isSuccessful() && r.body()!=null) adapter.setData(r.body());
            }
            @Override public void onFailure(Call<List<Product>> c, Throwable t) {
                swipeRefresh.setRefreshing(false);
                pb.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}