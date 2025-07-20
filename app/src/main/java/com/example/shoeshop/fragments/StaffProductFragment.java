package com.example.shoeshop.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.shoeshop.R;
import com.example.shoeshop.activities.AddNewProductActivity;
import com.example.shoeshop.adapters.StaffProductAdapter;
import com.example.shoeshop.models.Product;
import com.example.shoeshop.network.ApiService;
import com.example.shoeshop.network.ApiClient;
import com.example.shoeshop.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Collections;
import java.util.List;

public class StaffProductFragment extends Fragment {
    private RecyclerView rv;
    private StaffProductAdapter adapter;
    private ProgressBar pbLoading;
    private SwipeRefreshLayout swipeRefreshLayoutProducts;
    private ApiService api;
    private TextInputEditText etSearch;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_staff_products, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        swipeRefreshLayoutProducts = v.findViewById(R.id.swipeRefreshLayoutProducts);
        etSearch = v.findViewById(R.id.etSearch);
        TextInputLayout textInputLayoutSearch = v.findViewById(R.id.textInputLayoutSearch);
        pbLoading         = v.findViewById(R.id.pbLoadingProducts);
        MaterialButton btnAddProduct = v.findViewById(R.id.btnAddProduct);

        rv = v.findViewById(R.id.rvProducts);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new StaffProductAdapter(getContext(), new SessionManager(getContext()).getToken());
        rv.setAdapter(adapter);

        // Pull to refresh
        swipeRefreshLayoutProducts.setOnRefreshListener(() -> {
            String key = etSearch.getText().toString().trim();
            if (key.isEmpty()) loadProducts();
            else searchProducts(key);
            swipeRefreshLayoutProducts.setRefreshing(false);
        });

        // Add new
        btnAddProduct.setOnClickListener(x -> startActivity(new Intent(getContext(), AddNewProductActivity.class)));

        // Bắt sự kiện Enter/Search của bàn phím
        etSearch.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if ((actionId == EditorInfo.IME_ACTION_SEARCH)
                    || (keyEvent != null
                    && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER || keyEvent.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_ENTER)
                    && keyEvent.getAction() == KeyEvent.ACTION_DOWN) ) {
                performSearch();
                return true;
            }
            return false;
        });


        // Bắt sự kiện khi bấm icon search
        textInputLayoutSearch.setStartIconOnClickListener(view -> {
            performSearch(); // Gọi hàm xử lý tìm kiếm
        });

        // Initial load
        loadProducts();
    }
    private void performSearch() {
        String keyword = etSearch.getText().toString().trim();
        if (keyword.isEmpty()) {
            loadProducts();
        } else {
            searchProducts(keyword);
        }

        // Ẩn bàn phím
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        }
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
    private void searchProducts(String name) {
        pbLoading.setVisibility(View.VISIBLE);
        api   = ApiClient.getClient().create(ApiService.class);
        api.searchProducts( name, null, null, null, null)
                .enqueue(new Callback<List<Product>>() {
                    @Override public void onResponse(Call<List<Product>> c, Response<List<Product>> r) {
                        pbLoading.setVisibility(View.GONE);
                        if (r.isSuccessful() && r.body() != null) {
                            adapter.setData(r.body());
                        } else {
                            // Nếu không có kết quả
                            adapter.setData(Collections.emptyList());
                            Toast.makeText(getContext(), "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onFailure(Call<List<Product>> c, Throwable t) {
                        pbLoading.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}