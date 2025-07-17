package com.example.shoeshop.adapters;

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
import com.example.shoeshop.utils.SessionManager;
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
                .inflate(R.layout.staff_item_order_card2, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Order o = list.get(position);
        // Basic info
        h.tvOrderId.setText("Mã Đơn: " + o.getOrderID());

        try {
            String formatted = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    .format(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                            .parse(o.getOrderDate()));
            h.tvDate.setText("Ngày Đặt: " + formatted);
        } catch (Exception e) {
            h.tvDate.setText("Ngày Đặt: " + o.getOrderDate());
        }
        h.tvUserId.setText("Mã Khách hàng: " + o.getUserID());
        h.tvAddress.setText("Địa chỉ: " + o.getDeliveryAddress());
        h.tvPaymentMethod.setText("Phương thức: " + o.getMethodName());

        String total = NumberFormat.getInstance(new Locale("vi","VN")).format(o.getTotalAmount()) + " đ";
        h.tvPrice.setText("Tổng tiền: " + total);

        // First product
        List<Order.OrderDetail> details = o.getOrderDetails();
        if (details != null && !details.isEmpty()) {
            Order.OrderDetail d0 = details.get(0);
            h.imgProductThumb.setVisibility(View.VISIBLE);
            Glide.with(h.itemView.getContext())
                    .load(d0.getImageUrl())
                    .placeholder(R.drawable.placeholder)
                    .into(h.imgProductThumb);
            h.tvProductName.setText(d0.getProductName());
            String info = NumberFormat.getInstance(new Locale("vi","VN")).format(d0.getPrice()) + " x" + d0.getQuantity();
            h.tvProductInfo.setText(info);
        } else {
            h.imgProductThumb.setVisibility(View.GONE);
            h.tvProductName.setText("");
            h.tvProductInfo.setText("");
        }

        // Toggle extra products
        boolean hasMore = details != null && details.size() > 1;
        boolean isExpanded = expandedMap.get(position, false);
        h.btnToggleProducts.setVisibility(hasMore ? View.VISIBLE : View.GONE);
        h.btnToggleProducts.setText(isExpanded ? "Thu gọn ⌃" : "Xem thêm ˅");
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
                TextView tv = new TextView(h.itemView.getContext());
                tv.setText(d.getProductName() + "\n" +
                        NumberFormat.getInstance(new Locale("vi","VN")).format(d.getPrice()) + " x" + d.getQuantity());
                tv.setTextSize(14);
                row.addView(img);
                row.addView(tv);
                h.layoutExtraProducts.addView(row);
            }
        }
        h.btnToggleProducts.setOnClickListener(v -> {
            expandedMap.put(position, !expandedMap.get(position, false));
            notifyItemChanged(position);
        });

        // Reset action buttons
        h.btnNext.setVisibility(View.GONE);
        h.btnCancel.setVisibility(View.GONE);

        // Handle status-specific actions
        if ("ordered".equals(status)) {
            h.btnNext.setText("Đang thực hiện");
            h.btnCancel.setText("Hủy");
            h.btnNext.setVisibility(View.VISIBLE);
            h.btnCancel.setVisibility(View.VISIBLE);
            h.btnNext.setOnClickListener(v -> api.startProcessingOrder("Bearer "+token, o.getOrderID()).enqueue(callbackRemove(position)));
            h.btnCancel.setOnClickListener(v -> api.cancelOrder("Bearer "+token, o.getOrderID()).enqueue(callbackRemove(position)));
        } else if ("processing".equals(status)) {
            h.btnNext.setText("Chờ vận chuyển");
            h.btnCancel.setText("Hủy");
            h.btnNext.setVisibility(View.VISIBLE);
            h.btnCancel.setVisibility(View.VISIBLE);
            h.btnNext.setOnClickListener(v -> api.pendingShipOrder("Bearer "+token, o.getOrderID()).enqueue(callbackRemove(position)));
            h.btnCancel.setOnClickListener(v -> api.cancelOrder("Bearer "+token, o.getOrderID()).enqueue(callbackRemove(position)));
        } else if ("waiting-ship".equals(status)) {
            h.btnCancel.setText("Hủy");
            h.btnCancel.setVisibility(View.VISIBLE);
            h.btnCancel.setOnClickListener(v -> api.cancelOrder("Bearer "+token, o.getOrderID()).enqueue(callbackRemove(position)));
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
        ImageView imgProductThumb;
        TextView tvProductName, tvProductInfo, tvOrderId, tvStatus, tvDate, tvPrice, tvUserId, tvAddress, tvPaymentMethod, tvIsActive, btnToggleProducts;
        LinearLayout layoutExtraProducts;
        Button btnNext, btnCancel;

        VH(@NonNull View v) {
            super(v);
            imgProductThumb    = v.findViewById(R.id.imgProductThumb);
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
            btnToggleProducts  = v.findViewById(R.id.btnToggleProducts);
            layoutExtraProducts= v.findViewById(R.id.layoutExtraProducts);
            btnNext            = v.findViewById(R.id.btnNext);
            btnCancel          = v.findViewById(R.id.btnCancel);
        }
    }
}
