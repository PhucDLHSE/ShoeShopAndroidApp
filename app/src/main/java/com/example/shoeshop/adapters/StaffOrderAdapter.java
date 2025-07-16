package com.example.shoeshop.adapters;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.shoeshop.R;
import com.example.shoeshop.dialogs.OrderDetailsDialog;
import com.example.shoeshop.models.Order;
import com.example.shoeshop.models.StartOrderResponse;
import com.example.shoeshop.network.ApiService;
import com.example.shoeshop.network.ApiClient;
import com.example.shoeshop.utils.CustomDateAdapter;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.NumberFormat;
import java.util.List;

public class StaffOrderAdapter extends RecyclerView.Adapter<StaffOrderAdapter.VH> {
    private final List<Order> list;
    private final String token;
    private final String status;
    private final ApiService api;

    public StaffOrderAdapter(List<Order> data, String token, String status) {
        this.list = data; this.token = token; this.status = status;
        this.api = ApiClient.getClient().create(ApiService.class);
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        View vew = LayoutInflater.from(p.getContext())
                .inflate(R.layout.staff_item_order_card, p,false);
        return new VH(vew);
    }
    @Override public void onBindViewHolder(@NonNull VH h, int i) {
        Order o = list.get(i);
        h.tvOrderId.setText("Mã Đơn: "+o.getOrderID());
        h.tvUserId.setText("Mã Khách hàng: "+o.getUserID());
        try {
            h.tvDate.setText("Ngày Đặt: " + CustomDateAdapter.formatBackendDateForUI(o.getOrderDate()));
        } catch (Exception e) {
            // Fallback to raw date if formatting fails
            h.tvDate.setText("Ngày Đặt: " + o.getOrderDate());
        }
        NumberFormat formatter = NumberFormat.getInstance();
        h.tvPrice.setText("Tổng tiền: "+ formatter.format(o.getTotalAmount())+" đ");
        h.tvStatus.setText("Trạng thái: "+o.getStatus());
        h.tvAddress.setText("Địa chỉ: "+o.getDeliveryAddress());
        h.tvPaymentMethod.setText("Phương thức thanh toán: "+o.getMethodName());
        h.tvIsActive.setText("Trạng thái hoạt động: "+(o.isActive() ? "Hoạt động" : "Không hoạt động"));

        // Reset visibility
        h.btnNext.setVisibility(View.GONE);
        h.btnCancel.setVisibility(View.GONE);
        h.btnMore.setOnClickListener(v -> {
            new OrderDetailsDialog(v.getContext(), o.getOrderDetails())
                    .show();
        });

        switch(status){
            case "ordered":
                h.btnNext.setText("Đang thực hiện");
                h.btnCancel.setText("Hủy");
                h.btnNext.setVisibility(View.VISIBLE);
                h.btnCancel.setVisibility(View.VISIBLE);
                h.btnNext.setOnClickListener(v-> {
                    api.startProcessingOrder("Bearer "+token, o.getOrderID())
                            .enqueue(callbackRemove(i));
                });
                h.btnCancel.setOnClickListener(v-> {
                    api.cancelOrder("Bearer "+token, o.getOrderID())
                            .enqueue(callbackRemove(i));
                });
                break;
            case "processing":
                h.btnNext.setText("Chờ vận chuyển");
                h.btnCancel.setText("Hủy");
                h.btnNext.setVisibility(View.VISIBLE);
                h.btnCancel.setVisibility(View.VISIBLE);
                h.btnNext.setOnClickListener(v-> {
                    api.pendingShipOrder("Bearer "+token, o.getOrderID())
                            .enqueue(callbackRemove(i));
                });
                h.btnCancel.setOnClickListener(v-> {
                    api.cancelOrder("Bearer "+token, o.getOrderID())
                            .enqueue(callbackRemove(i));
                });
                break;
            case "waiting-ship":
                h.btnCancel.setText("Hủy");
                h.btnCancel.setVisibility(View.VISIBLE);
                h.btnCancel.setOnClickListener(v-> {
                    api.cancelOrder("Bearer "+token, o.getOrderID())
                            .enqueue(callbackRemove(i));
                });
                break;
            // status "shipping","completed","cancled": không có button
        }
    }

    private Callback<StartOrderResponse> callbackRemove(int pos){
        return new Callback<>() {
            @Override public void onResponse(Call<StartOrderResponse> c, Response<StartOrderResponse> r){
                if(r.isSuccessful()){ list.remove(pos); notifyItemRemoved(pos); }
            }
            @Override public void onFailure(Call<StartOrderResponse> c, Throwable t){}
        };
    }

    @Override public int getItemCount(){ return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvOrderId,tvDate,tvPrice, tvUserId, tvStatus, tvAddress, tvPaymentMethod, tvIsActive;
        Button btnNext,btnCancel;
        ImageView btnMore;
        public VH(@NonNull View v) {
            super(v);
            tvOrderId = v.findViewById(R.id.tvOrderId);
            tvUserId  = v.findViewById(R.id.tvUserId);
            tvDate    = v.findViewById(R.id.tvDate);
            tvPrice   = v.findViewById(R.id.tvPrice);
            tvStatus  = v.findViewById(R.id.tvStatus);
            tvAddress = v.findViewById(R.id.tvAddress);
            tvPaymentMethod = v.findViewById(R.id.tvPaymentMethod);
            tvIsActive = v.findViewById(R.id.tvIsActive);
            btnNext   = v.findViewById(R.id.btnNext);
            btnCancel = v.findViewById(R.id.btnCancel);
            btnMore = v.findViewById(R.id.btnMore);
        }
    }
}