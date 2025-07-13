package com.example.shoeshop.adapters;

import android.graphics.Typeface;
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

import java.util.ArrayList;
import java.util.List;

public class DeliveryAdapter extends RecyclerView.Adapter<DeliveryAdapter.VH> {
    private final List<DeliveryStatusResponse> list;
    private final String token, status;
    private final ApiService api;

    public DeliveryAdapter(List<DeliveryStatusResponse> data, String token, String status){
        this.list=new ArrayList<>(data);
        this.token=token;
        this.status=status;
        this.api = ApiClient.getClient().create(ApiService.class);
    }

    /** Cập nhật dữ liệu mới và refresh list **/
    public void updateData(List<DeliveryStatusResponse> newList) {
        list.clear();
        list.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int v){
        View vew = LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_delivery_card,p,false);
        return new VH(vew);
    }
    @Override public void onBindViewHolder(@NonNull VH h, int i){
        var d = list.get(i);
        h.tvDeliveryId.setText("DeliveryID: "+d.getDeliveryID());
        h.tvDeliveryId.setTypeface(null, Typeface.BOLD);
        h.tvOrderId.setText("OrderID: "+d.getOrderID());
        h.tvShipperName.setText("Shipper: "+d.getShipperName());
        h.tvDeliveryStatus.setText("Delivery Status: "+d.getDeliveryStatus());
        h.tvDeliveryAddress.setText("Delivery Address: "+d.getDeliveryAddress());
        h.tvShipperPhone.setText("Shipper Phone: "+d.getShipperPhone());
        h.tvDeliveryDate.setText("Delivery Date: "+d.getDeliveryDate());
        h.tvStorageName.setText("Storage Name: "+d.getStorageName());
        h.tvNote.setText("Note: "+d.getNote());
        h.tvIsActive.setText("Active: "+(d.isActive() ? "True" : "False"));

        h.btnNext.setVisibility(View.GONE);
        h.btnCancel.setVisibility(View.GONE);

        switch(status){
            case "chờ lấy hàng":
                h.btnNext.setText("Đang giao hàng");
                h.btnNext.setVisibility(View.VISIBLE);
                h.btnNext.setOnClickListener(v->{
                    api.startDelivery("Bearer "+token, d.getDeliveryID())
                            .enqueue(callbackRemove(i));
                });
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
                h.btnNext.setOnClickListener(v->{
                    api.completeDelivery("Bearer "+token, d.getDeliveryID())
                            .enqueue(callbackRemove(i));
                });
                break;
            case "đã giao hàng":
                // Không có nút nào, chỉ hiển thị thông tin
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
        TextView tvDeliveryId, tvOrderId, tvShipperName, tvDeliveryStatus, tvDeliveryAddress, tvShipperPhone, tvDeliveryDate, tvStorageName, tvNote, tvIsActive;
        Button btnNext, btnCancel;
        public VH(@NonNull View v){
            super(v);
            tvDeliveryId   = v.findViewById(R.id.tvDeliveryId);
            tvOrderId      = v.findViewById(R.id.tvOrderId);
            tvShipperName  = v.findViewById(R.id.tvShipperName);
            tvDeliveryStatus = v.findViewById(R.id.tvDeliveryStatus);
            tvDeliveryAddress = v.findViewById(R.id.tvDeliveryAddress);
            tvShipperPhone = v.findViewById(R.id.tvShipperPhone);
            tvDeliveryDate = v.findViewById(R.id.tvDeliveryDate);
            tvStorageName  = v.findViewById(R.id.tvStorageName);
            tvNote         = v.findViewById(R.id.tvNote);
            tvIsActive     = v.findViewById(R.id.tvIsActive);
            btnNext        = v.findViewById(R.id.btnNext);
            btnCancel      = v.findViewById(R.id.btnCancel);
        }
    }
}