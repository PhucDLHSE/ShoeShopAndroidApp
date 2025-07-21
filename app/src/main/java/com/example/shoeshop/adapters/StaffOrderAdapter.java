package com.example.shoeshop.adapters;

import android.graphics.Color;
import android.util.SparseBooleanArray;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.shoeshop.R;
import com.example.shoeshop.models.Order;
import com.example.shoeshop.models.StartOrderResponse;
import com.example.shoeshop.network.ApiService;
import com.example.shoeshop.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class StaffOrderAdapter extends RecyclerView.Adapter<StaffOrderAdapter.VH> {
    private final List<Order> list;
    private final String token;
    private final String status;
    private final ApiService api;
    private final SparseBooleanArray expandedMap = new SparseBooleanArray();

    public StaffOrderAdapter(List<Order> data, String token, String status) {
        this.list = data;
        this.token = token;
        this.status = status;
        this.api = ApiClient.getClient().create(ApiService.class);
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.staff_item_order_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Order o = list.get(position);
        // Basic info
        String orderId = o.getOrderID();
        if (orderId != null && orderId.length() > 8) {
            orderId = orderId.substring(0, 8) ;
        }
        h.tvOrderId.setText("Mã Đơn: "+orderId);
        h.tvStatus.setText(o.getStatus());

        try {
            String formatted = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                    .format(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(o.getOrderDate()));
            h.tvDate.setText("Ngày Đặt: " + formatted);
        } catch (Exception e) {
            h.tvDate.setText("Ngày Đặt: " + o.getOrderDate());
        }
        String userId = o.getUserID();
        if(userId != null && userId.length() > 8) {
            userId = userId.substring(0, 8) ;
        };
        h.tvUserId.setText("Mã Khách Hàng: "+userId);
        h.tvAddress.setText("Địa chỉ: " + o.getDeliveryAddress());
        h.tvPaymentMethod.setText("Phương thức: " + o.getMethodName());

        String total = NumberFormat.getInstance(new Locale("vi","VN")).format(o.getTotalAmount()) + " đ";
        h.tvPrice.setText("Tổng tiền: " + total);

        // First product
        List<Order.OrderDetail> details = o.getOrderDetails();
        if (details != null && !details.isEmpty()) {
            Order.OrderDetail d0 = details.get(0);
            h.ivProductThumb.setVisibility(View.VISIBLE);
            Glide.with(h.itemView.getContext())
                    .load(d0.getImageUrl())
                    .placeholder(R.drawable.placeholder)
                    .into(h.ivProductThumb);
            h.tvProductName.setText(d0.getProductName());
            String info = NumberFormat.getInstance(new Locale("vi","VN")).format(d0.getPrice()) + " x" + d0.getQuantity();
            h.tvProductInfo.setText(info);
        } else {
            h.ivProductThumb.setVisibility(View.GONE);
            h.tvProductName.setText("");
            h.tvProductInfo.setText("");
        }

        // Toggle extra products
        boolean hasMore = details != null && details.size() > 1;
        boolean isExpanded = expandedMap.get(position, false);
        if (hasMore) {
            h.layoutToggleProducts.setVisibility(View.VISIBLE);

            if (isExpanded) {
                h.tvToggleProducts.setText("Thu gọn");
                h.ivToggleProducts.setImageResource(R.drawable.ic_chevron_up);
            } else {
                h.tvToggleProducts.setText("Xem thêm");
                h.ivToggleProducts.setImageResource(R.drawable.ic_chevron_down);
            }

        } else {
            h.layoutToggleProducts.setVisibility(View.GONE);
        }

        h.layoutExtraProducts.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        if (isExpanded && hasMore) {
            h.layoutExtraProducts.removeAllViews();
            for (int i = 1; i < details.size(); i++) {
                Order.OrderDetail d = details.get(i);

                LinearLayout row = new LinearLayout(h.itemView.getContext());
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(0, 8, 0, 8);

                ImageView img = new ImageView(h.itemView.getContext());
                img.setLayoutParams(new LinearLayout.LayoutParams(
                        (int)(90*h.itemView.getResources().getDisplayMetrics().density),
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                img.setAdjustViewBounds(true);
                img.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Glide.with(img.getContext()).load(d.getImageUrl())
                        .placeholder(R.drawable.placeholder).into(img);

                // Create a vertical layout for texts
                LinearLayout textLayout = new LinearLayout(h.itemView.getContext());
                textLayout.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams textLayoutParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                textLayoutParams.setMargins((int)(14 * h.itemView.getResources().getDisplayMetrics().density), 0, 0, 0);
                textLayout.setLayoutParams(textLayoutParams);

                TextView tvProductName = new TextView(h.itemView.getContext());
                tvProductName.setText(d.getProductName());
                tvProductName.setTextSize(14);

                TextView tvProductInfo = new TextView(h.itemView.getContext());
                tvProductInfo.setText(NumberFormat.getInstance(new Locale("vi", "VN")).format(d.getPrice()) + " x" + d.getQuantity());
                tvProductInfo.setTextSize(13);
                tvProductInfo.setTextColor(Color.GRAY);

                textLayout.addView(tvProductName);
                textLayout.addView(tvProductInfo);


                row.addView(img);
                row.addView(textLayout);

                h.layoutExtraProducts.addView(row);
            }
        }
        h.layoutToggleProducts.setOnClickListener(v -> {
            expandedMap.put(position, !expandedMap.get(position, false));
            notifyItemChanged(position);
        });

        // Reset action buttons
        h.ivNext.setVisibility(View.GONE);
        h.ivCancel.setVisibility(View.GONE);

        // Handle status-specific actions
        if ("ordered".equals(status)) {
            h.ivNext.setVisibility(View.VISIBLE);
            h.ivCancel.setVisibility(View.VISIBLE);
            h.ivNext.setOnClickListener(v -> api.startProcessingOrder("Bearer "+token, o.getOrderID()).enqueue(callbackRemove(position)));
            h.ivCancel.setOnClickListener(v -> api.cancelOrder("Bearer "+token, o.getOrderID()).enqueue(callbackRemove(position)));
        } else if ("processing".equals(status)) {
            h.ivNext.setVisibility(View.VISIBLE);
            h.ivCancel.setVisibility(View.VISIBLE);
            h.ivNext.setOnClickListener(v -> api.pendingShipOrder("Bearer "+token, o.getOrderID()).enqueue(callbackRemove(position)));
            h.ivCancel.setOnClickListener(v -> api.cancelOrder("Bearer "+token, o.getOrderID()).enqueue(callbackRemove(position)));
        } else if ("waiting-ship".equals(status)) {
            h.ivCancel.setVisibility(View.VISIBLE);
            h.ivCancel.setOnClickListener(v -> api.cancelOrder("Bearer "+token, o.getOrderID()).enqueue(callbackRemove(position)));
        }
    }

    private Callback<StartOrderResponse> callbackRemove(int pos) {
        return new Callback<StartOrderResponse>() {
            @Override public void onResponse(Call<StartOrderResponse> c, Response<StartOrderResponse> r) {
                if (r.isSuccessful()) { list.remove(pos); notifyItemRemoved(pos); }
            }
            @Override public void onFailure(Call<StartOrderResponse> c, Throwable t) {

            }
        };
    }

    @Override public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivProductThumb, ivToggleProducts, ivCancel, ivNext;
        TextView tvProductName, tvProductInfo, tvOrderId, tvStatus, tvDate, tvPrice, tvUserId, tvAddress, tvPaymentMethod, tvIsActive, tvToggleProducts ;
        LinearLayout layoutExtraProducts, layoutToggleProducts;
        VH(@NonNull View v) {
            super(v);
            ivProductThumb    = v.findViewById(R.id.ivProductThumb);
            tvProductName      = v.findViewById(R.id.tvProductName);
            tvProductInfo      = v.findViewById(R.id.tvProductInfo);
            tvOrderId          = v.findViewById(R.id.tvOrderId);
            tvStatus           = v.findViewById(R.id.tvStatus);
            tvDate             = v.findViewById(R.id.tvDate);
            tvPrice            = v.findViewById(R.id.tvPrice);
            tvUserId           = v.findViewById(R.id.tvUserId);
            tvAddress          = v.findViewById(R.id.tvAddress);
            tvPaymentMethod    = v.findViewById(R.id.tvPaymentMethod);
            tvIsActive         = v.findViewById(R.id.tvIsActive);
            tvToggleProducts  = v.findViewById(R.id.tvToggleProducts);
            layoutExtraProducts= v.findViewById(R.id.layoutExtraProducts);
            layoutToggleProducts = v.findViewById(R.id.layoutToggleProducts);
            ivNext            = v.findViewById(R.id.ivNext);
            ivCancel          = v.findViewById(R.id.ivCancel);
            ivToggleProducts = v.findViewById(R.id.ivToggleProducts);
        }
    }
}
