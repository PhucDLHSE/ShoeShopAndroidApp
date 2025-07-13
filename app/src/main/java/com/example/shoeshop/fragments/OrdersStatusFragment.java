package com.example.shoeshop.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.ProgressBar;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.example.shoeshop.R;
import com.example.shoeshop.adapters.StaffOrderAdapter;
import com.example.shoeshop.network.ApiService;
import com.example.shoeshop.network.ApiClient;
import com.example.shoeshop.utils.SessionManager;
import com.example.shoeshop.models.Order;
import com.example.shoeshop.models.StartOrderResponse;
import retrofit2.*;
import java.util.*;

public class OrdersStatusFragment extends Fragment {
    private final String status;
    private RecyclerView rv;
    private ProgressBar spinner;
    private StaffOrderAdapter adapter;
    private final List<Order> data = new ArrayList<>();

    public OrdersStatusFragment(String status) {
        this.status = status;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle b) {
        return i.inflate(R.layout.fragment_orders_status, c, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);

        rv = v.findViewById(R.id.rvOrders);
        spinner = v.findViewById(R.id.pbStatusLoading);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        String token = new SessionManager(getContext()).getToken();
        adapter = new StaffOrderAdapter(data, token, status);
        rv.setAdapter(adapter);

        loadData();
    }

    private void loadData() {
        if (getContext() == null) return;
        if (spinner != null) spinner.setVisibility(View.VISIBLE);
        String token = new SessionManager(getContext()).getToken();
        ApiService api = ApiClient.getClient().create(ApiService.class);
        Call<List<Order>> call;
        switch (status) {
            case "ordered":      call = api.getOrderedOrders("Bearer "+token); break;
            case "processing":   call = api.getProcessingOrders("Bearer "+token); break;
            case "waiting-ship": call = api.getWaitingShipOrders("Bearer "+token); break;
            case "shipping":     call = api.getShippingOrders("Bearer "+token); break;
            case "completed":    call = api.getCompleteOrders("Bearer "+token); break;
            default:             call = api.getCanceledOrders("Bearer "+token); break;
        }

        call.enqueue(new Callback<>() {
            @Override public void onResponse(Call<List<Order>> c, Response<List<Order>> r) {
                if (spinner != null) spinner.setVisibility(View.GONE);
                if (r.isSuccessful() && r.body() != null) {
                    data.clear();
                    data.addAll(r.body());
                    adapter.notifyDataSetChanged();
                }
            }
            @Override public void onFailure(Call<List<Order>> c, Throwable t) {
                if (spinner != null) spinner.setVisibility(View.GONE);
                // có thể show Toast
            }
        });
    }

    /** Gọi từ parent để reload lại dữ liệu **/
    public void reloadData() {
        loadData();
    }
}