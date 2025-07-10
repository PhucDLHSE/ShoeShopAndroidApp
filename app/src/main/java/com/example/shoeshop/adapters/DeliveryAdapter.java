package com.example.shoeshop.adapters;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.shoeshop.R;
import com.example.shoeshop.models.DeliveryStatusResponse;
import com.example.shoeshop.models.PatchDeliveryResponse;
import com.example.shoeshop.network.ApiService;
import com.example.shoeshop.network.ApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class DeliveryAdapter extends RecyclerView.Adapter<DeliveryAdapter.VH> {
    private final List<DeliveryStatusResponse> list;
    private final String token, status;
    private final ApiService api;

    public DeliveryAdapter(List<DeliveryStatusResponse> data, String token, String status){
        this.list=data; this.token=token; this.status=status;
        this.api = ApiClient.getClient().create(ApiService.class);
    }
    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int v){
        View vew = LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_delivery_card,p,false);
        return new VH(vew);
    }
    @Override public void onBindViewHolder(@NonNull VH h, int i){
        var d = list.get(i);
        h.tvDeliveryId.setText("Phiếu #"+d.getDeliveryID());
        h.tvOrderId.setText("Đơn #"+d.getOrderID());
        h.tvShipperName.setText(d.getShipperName());

        h.btnNext.setVisibility(View.GONE);
        h.btnCancel.setVisibility(View.GONE);

        switch(status){
            case "chờ lấy hàng":
                // chỉ hiển thị button Cancel để Hủy phiếu
                h.btnCancel.setText("Hủy");
                h.btnCancel.setVisibility(View.VISIBLE);
                h.btnCancel.setOnClickListener(v->{
                    api.cancelDelivery("Bearer "+token, d.getDeliveryID())
                            .enqueue(callbackRemove(i));
                });
                break;
            case "đang giao hàng":
                h.btnNext.setText("Hoàn thành");
                h.btnNext.setVisibility(View.VISIBLE);
                h.btnCancel.setText("Hủy");
                h.btnCancel.setVisibility(View.VISIBLE);
                h.btnNext.setOnClickListener(v->{
                    api.completeDelivery("Bearer "+token, d.getDeliveryID())
                            .enqueue(callbackRemove(i));
                });
                h.btnCancel.setOnClickListener(v->{
                    api.cancelDelivery("Bearer "+token, d.getDeliveryID())
                            .enqueue(callbackRemove(i));
                });
                break;
            case "đã giao hàng":
                h.btnCancel.setText("Hủy");
                h.btnCancel.setVisibility(View.VISIBLE);
                h.btnCancel.setOnClickListener(v->{
                    api.cancelDelivery("Bearer "+token, d.getDeliveryID())
                            .enqueue(callbackRemove(i));
                });
                break;
        }
    }
    private Callback<PatchDeliveryResponse> callbackRemove(int pos){
        return new Callback<PatchDeliveryResponse>(){
            @Override public void onResponse(Call<PatchDeliveryResponse> c, Response<PatchDeliveryResponse> r){
                if(r.isSuccessful()){ list.remove(pos); notifyItemRemoved(pos); }
            }
            @Override public void onFailure(Call<PatchDeliveryResponse> c, Throwable t){}
        };
    }
    @Override public int getItemCount(){ return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDeliveryId, tvOrderId, tvShipperName;
        Button btnNext, btnCancel;
        public VH(@NonNull View v){
            super(v);
            tvDeliveryId   = v.findViewById(R.id.tvDeliveryId);
            tvOrderId      = v.findViewById(R.id.tvOrderId);
            tvShipperName  = v.findViewById(R.id.tvShipperName);
            btnNext        = v.findViewById(R.id.btnNext);
            btnCancel      = v.findViewById(R.id.btnCancel);
        }
    }
}