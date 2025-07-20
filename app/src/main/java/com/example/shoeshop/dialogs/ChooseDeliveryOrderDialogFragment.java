package com.example.shoeshop.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeshop.R;
import com.example.shoeshop.adapters.ChooseOrderAdapter;
import com.example.shoeshop.models.Order;
import com.example.shoeshop.network.ApiClient;
import com.example.shoeshop.network.ApiService;
import com.example.shoeshop.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChooseDeliveryOrderDialogFragment extends DialogFragment {

    private RecyclerView rv;
    private Button btnSave, btnCancel;
    private String selectedOrderId = null;
    private ChooseOrderAdapter adapter;
    private OnOrderChosenListener listener;

    public interface OnOrderChosenListener {
        void onOrderChosen(String orderId, String display);
    }

    public ChooseDeliveryOrderDialogFragment(OnOrderChosenListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_choose_delivery_order, container, false);
        rv = view.findViewById(R.id.rvChooseOrders);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        String token = new SessionManager(requireContext()).getToken();
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getWaitingShipOrders("Bearer " + token)
                .enqueue(new Callback<List<Order>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Order>> call,
                                           @NonNull Response<List<Order>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Order> list = response.body();
                            adapter = new ChooseOrderAdapter(list, id -> selectedOrderId = id);
                            rv.setAdapter(adapter);
                        } else {
                            Toast.makeText(getContext(), "Lấy danh sách đơn thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Order>> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {
            if (selectedOrderId == null) {
                Toast.makeText(getContext(), "Chưa chọn đơn", Toast.LENGTH_SHORT).show();
                return;
            }
            listener.onOrderChosen(selectedOrderId, "Đơn " + selectedOrderId.substring(0, 8));
            dismiss();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
