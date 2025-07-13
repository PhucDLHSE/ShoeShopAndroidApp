package com.example.shoeshop.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.ProgressBar;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.example.shoeshop.R;
import com.example.shoeshop.adapters.DeliveryAdapter;
import com.example.shoeshop.network.ApiService;
import com.example.shoeshop.network.ApiClient;
import com.example.shoeshop.network.ApiService;
import com.example.shoeshop.utils.SessionManager;
import com.example.shoeshop.models.*;
import retrofit2.*;

import java.util.ArrayList;
import java.util.List;

public class DeliveryStatusFragment extends Fragment {
    private final String status;
    private RecyclerView rv;
    private ProgressBar spinner;
    private DeliveryAdapter adapter;

    public DeliveryStatusFragment(String status) {
        this.status = status;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle b) {
        return i.inflate(R.layout.fragment_delivery_status, c, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);
        rv = v.findViewById(R.id.rvDelivery);
        spinner = v.findViewById(R.id.pbTabLoading);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DeliveryAdapter(new ArrayList<>(), new SessionManager(getContext()).getToken(), status);
        rv.setAdapter(adapter);
        loadData();
    }

    private void loadData() {
        if (getContext() == null) return;
        if (spinner != null) spinner.setVisibility(View.VISIBLE);
        String token = new SessionManager(getContext()).getToken();
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getDeliveriesByStatus("Bearer "+token, status)
                .enqueue(new Callback<List<DeliveryStatusResponse>>() {
                    @Override public void onResponse(Call<List<DeliveryStatusResponse>> c,
                                                     Response<List<DeliveryStatusResponse>> r) {
                        if (spinner != null) spinner.setVisibility(View.GONE);
                        if (r.isSuccessful() && r.body()!=null) {
                            adapter.updateData(r.body());
                        }
                    }
                    @Override public void onFailure(Call<List<DeliveryStatusResponse>> c, Throwable t) {
                        if (spinner != null) spinner.setVisibility(View.GONE);
                    }
                });
    }

    /** Gọi từ parent để reload lại dữ liệu **/
    public void reloadData() {
        loadData();
    }
}