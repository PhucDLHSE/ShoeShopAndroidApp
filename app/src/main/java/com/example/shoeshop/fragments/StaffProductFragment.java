package com.example.shoeshop.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.shoeshop.R;
import com.example.shoeshop.activities.AddNewProductActivity;
import com.example.shoeshop.activities.SearchProductActivity;
import com.example.shoeshop.adapters.StaffProductAdapter;
import com.example.shoeshop.models.Product;
import com.example.shoeshop.network.ApiService;
import com.example.shoeshop.network.ApiClient;
import com.example.shoeshop.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

public class StaffProductFragment extends Fragment {
    private RecyclerView rv;
    private StaffProductAdapter adapter;
    private ProgressBar pbLoading;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_staff_products, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        Button btnRefresh = v.findViewById(R.id.btnRefreshProducts);
        Button btnSearch  = v.findViewById(R.id.btnSearchProducts);
        Button btnAdd     = v.findViewById(R.id.btnAddProduct);
        pbLoading         = v.findViewById(R.id.pbLoadingProducts);

        rv = v.findViewById(R.id.rvProducts);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new StaffProductAdapter(getContext(), new SessionManager(getContext()).getToken());
        rv.setAdapter(adapter);

        // refresh list
        btnRefresh.setOnClickListener(x -> loadProducts());

        // search
        btnSearch.setOnClickListener(x -> startActivity(new Intent(getContext(), SearchProductActivity.class)));

        // add new
        btnAdd.setOnClickListener(x -> startActivity(new Intent(getContext(), AddNewProductActivity.class)));

        // initial load
        loadProducts();
    }

    private void loadProducts() {
        pbLoading.setVisibility(View.VISIBLE);
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getAllProducts()
                .enqueue(new Callback<List<Product>>() {
                    @Override public void onResponse(Call<List<Product>> c, Response<List<Product>> r) {
                        pbLoading.setVisibility(View.GONE);
                        if(r.isSuccessful() && r.body()!=null) {
                            adapter.setData(r.body());
                        }
                    }
                    @Override public void onFailure(Call<List<Product>> c, Throwable t) {
                        pbLoading.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}