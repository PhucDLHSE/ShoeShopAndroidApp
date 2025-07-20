package com.example.shoeshop.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.shoeshop.R;
import com.example.shoeshop.models.CreateDeliveryRequest;
import com.example.shoeshop.models.PatchDeliveryResponse;
import com.example.shoeshop.network.ApiClient;
import com.example.shoeshop.network.ApiService;
import com.example.shoeshop.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateDeliveryDialogFragment extends DialogFragment {

    private Button btnChooseOrder, btnCreate, btnCancel;
    private String chosenOrderId = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_create_delivery, container, false);

        btnChooseOrder = view.findViewById(R.id.btnChooseOrder);
        btnCreate      = view.findViewById(R.id.btnCreate);
        btnCancel      = view.findViewById(R.id.btnCancel);

        btnChooseOrder.setOnClickListener(v -> {
            ChooseDeliveryOrderDialogFragment chooseDialog =
                    new ChooseDeliveryOrderDialogFragment((orderId, display) -> {
                        chosenOrderId = orderId;
                        btnChooseOrder.setText(display);
                    });
            chooseDialog.show(getParentFragmentManager(), "choose_order");
        });


        btnCancel.setOnClickListener(v -> dismiss());

        btnCreate.setOnClickListener(v -> {
            if (chosenOrderId == null) {
                Toast.makeText(getContext(), "Chưa chọn đơn hàng", Toast.LENGTH_SHORT).show();
                return;
            }

            String token = new SessionManager(requireContext()).getToken();
            String shipper = ((EditText) view.findViewById(R.id.etShipperName)).getText().toString();
            String phone   = ((EditText) view.findViewById(R.id.etShipperPhone)).getText().toString();
            String note    = ((EditText) view.findViewById(R.id.etNote)).getText().toString();

            CreateDeliveryRequest req = new CreateDeliveryRequest(chosenOrderId, shipper, phone, note);
            ApiService api = ApiClient.getClient().create(ApiService.class);

            api.createDelivery("Bearer " + token, req).enqueue(new Callback<PatchDeliveryResponse>() {
                @Override
                public void onResponse(Call<PatchDeliveryResponse> call, Response<PatchDeliveryResponse> resp) {
                    Toast.makeText(getContext(), "Tạo phiếu giao hàng thành công", Toast.LENGTH_SHORT).show();
                    dismiss(); // Đóng dialog sau khi tạo
                }

                @Override
                public void onFailure(Call<PatchDeliveryResponse> call, Throwable t) {
                    Toast.makeText(getContext(), "Lỗi tạo phiếu", Toast.LENGTH_SHORT).show();
                }
            });
        });

        return view;
    }

    @Override
    public void onActivityResult(int req, int res, @Nullable Intent data) {
        super.onActivityResult(req, res, data);
        if (req == 100 && res == Activity.RESULT_OK && data != null) {
            chosenOrderId = data.getStringExtra("orderId");
            String display = data.getStringExtra("orderSummary");
            btnChooseOrder.setText(display);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Giúp dialog mở rộng full width
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
