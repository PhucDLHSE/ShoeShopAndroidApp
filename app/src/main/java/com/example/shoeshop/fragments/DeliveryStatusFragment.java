package com.example.shoeshop.fragments;

import android.os.Bundle;
import android.view.*;
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
import java.util.List;

public class DeliveryStatusFragment extends Fragment {
    private final String status;
    public DeliveryStatusFragment(String status){ this.status=status; }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle b){
        return i.inflate(R.layout.fragment_delivery_status, c, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s){
        RecyclerView rv = v.findViewById(R.id.rvDelivery);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        String token = new SessionManager(getContext()).getToken();
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getDeliveriesByStatus("Bearer "+token, status)
                .enqueue(new Callback<List<DeliveryStatusResponse>>() {
                    @Override public void onResponse(Call<List<DeliveryStatusResponse>> c, Response<List<DeliveryStatusResponse>> r){
                        if(r.isSuccessful()&&r.body()!=null){
                            DeliveryAdapter ad = new DeliveryAdapter(r.body(), token, status);
                            rv.setAdapter(ad);
                        }
                    }
                    @Override public void onFailure(Call<List<DeliveryStatusResponse>> c, Throwable t){}
                });
    }
}