package com.example.shoeshop.adapters;

import android.graphics.Color;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shoeshop.R;
import com.example.shoeshop.models.Order;
import com.example.shoeshop.utils.CustomDateAdapter;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class ChooseOrderAdapter extends RecyclerView.Adapter<ChooseOrderAdapter.ViewHolder> {
    private final List<Order> orders;
    private final Consumer<String> onSelect;
    private int selectedPosition = -1;
    private final SparseBooleanArray expandedMap = new SparseBooleanArray();

    public ChooseOrderAdapter(List<Order> orders, Consumer<String> onSelect) {
        this.orders = orders;
        this.onSelect = onSelect;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.staff_item_order_card, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);
        List<Order.OrderDetail> details = order.getOrderDetails();
        // Bind data
        holder.tvOrderId.setText("Mã Đơn: " + order.getOrderID().substring(0,8));
        holder.tvUserId.setText("Người dùng: " + order.getUserID().substring(0,8));
//        try{
//            holder.tvDate.setText("Ngày Đặt: " + CustomDateAdapter.formatBackendDateForUI(order.getOrderDate()));
//        } catch (Exception e) {
//            holder.tvDate.setText("Ngày Đặt: " + order.getOrderDate()); //Fallback
//        }
        try {
            String formatted = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                    .format(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(order.getOrderDate()));
            holder.tvDate.setText("Ngày Đặt: " + formatted);
        } catch (Exception e) {
            holder.tvDate.setText("Ngày Đặt: " + order.getOrderDate());
        }
        holder.tvStatus.setText(order.getStatus());
        DecimalFormat formatter = new DecimalFormat("#,###");
        holder.tvPrice.setText("Tổng Đơn: "+ formatter.format(order.getTotalAmount()) + " đ");
        holder.tvAddress.setText("Địa Chỉ Giao Hàng: "+ order.getDeliveryAddress());
        holder.tvPaymentMethod.setText("Phương Thức Thanh Toán: "+ order.getMethodName());

        holder.btnCancel.setVisibility(View.GONE);
        holder.btnNext.setVisibility(View.GONE);

        // Highlight selected
        holder.itemView.setBackgroundColor(
                position == selectedPosition ? Color.LTGRAY : Color.WHITE
        );

        holder.itemView.setOnClickListener(v -> {
            int previous = selectedPosition;
            selectedPosition = holder.getBindingAdapterPosition();
            notifyItemChanged(previous);
            notifyItemChanged(selectedPosition);
            onSelect.accept(order.getOrderID());
        });

        //  Bind sản phẩm đầu tiên
        if (details != null && !details.isEmpty()) {
            Order.OrderDetail d0 = details.get(0);
            holder.ivProductThumb.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(d0.getImageUrl())
                    .placeholder(R.drawable.placeholder)
                    .into(holder.ivProductThumb);
            holder.tvProductName.setText(d0.getProductName());
            String info0 = NumberFormat.getInstance(new Locale("vi","VN"))
                    .format(d0.getPrice()) + " x" + d0.getQuantity();
            holder.tvProductInfo.setText(info0);
        } else {
            holder.ivProductThumb.setVisibility(View.GONE);
            holder.tvProductName.setText("");
            holder.tvProductInfo.setText("");
        }

        //  Logic expand/collapse extra products
        boolean hasMore = details != null && details.size() > 1;
        boolean isExp   = expandedMap.get(position, false);

        if (hasMore) {
            holder.layoutToggleProducts.setVisibility(View.VISIBLE);
            holder.tvToggleProducts.setText(isExp ? "Thu gọn" : "Xem thêm");
            holder.ivToggleProducts.setImageResource(
                    isExp ? R.drawable.ic_chevron_up : R.drawable.ic_chevron_down
            );
        } else {
            holder.layoutToggleProducts.setVisibility(View.GONE);
        }

        holder.layoutExtraProducts.setVisibility(isExp ? View.VISIBLE : View.GONE);
        if (isExp && hasMore) {
            holder.layoutExtraProducts.removeAllViews();
            for (int i = 1; i < details.size(); i++) {
                Order.OrderDetail d = details.get(i);
                LinearLayout row = new LinearLayout(holder.itemView.getContext());
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(0,8,0,8);

                ImageView img = new ImageView(holder.itemView.getContext());
                int px = (int)(90*holder.itemView.getResources().getDisplayMetrics().density);
                row.addView(img, new LinearLayout.LayoutParams(px,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                img.setAdjustViewBounds(true);
                img.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Glide.with(img.getContext())
                        .load(d.getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(img);

                LinearLayout textLayouts = new LinearLayout(holder.itemView.getContext());
                textLayouts.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.setMargins((int)(14*holder.itemView.getResources()
                        .getDisplayMetrics().density),0,0,0);
                textLayouts.setLayoutParams(lp);

                TextView tName = new TextView(holder.itemView.getContext());
                tName.setText(d.getProductName());
                tName.setTextSize(14);

                TextView tInfo = new TextView(holder.itemView.getContext());
                tInfo.setText(NumberFormat.getInstance(new Locale("vi","VN"))
                        .format(d.getPrice()) + " x" + d.getQuantity());
                tInfo.setTextSize(13);
                tInfo.setTextColor(Color.GRAY);

                textLayouts.addView(tName);
                textLayouts.addView(tInfo);
                row.addView(textLayouts);

                holder.layoutExtraProducts.addView(row);
            }
        }

        holder.layoutToggleProducts.setOnClickListener(v -> {
            expandedMap.put(position, !isExp);
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvUserId, tvDate, tvStatus,
                tvPrice, tvAddress, tvPaymentMethod, tvIsActive, tvProductName, tvProductInfo, tvToggleProducts;
        ImageView ivProductThumb,ivToggleProducts;
        LinearLayout layoutToggleProducts, layoutExtraProducts;
        Button btnNext, btnCancel;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvUserId = itemView.findViewById(R.id.tvUserId);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
            tvIsActive = itemView.findViewById(R.id.tvIsActive);
            // First product
            ivProductThumb  = itemView.findViewById(R.id.ivProductThumb);
            tvProductName   = itemView.findViewById(R.id.tvProductName);
            tvProductInfo   = itemView.findViewById(R.id.tvProductInfo);
            //  Toggle & extra list
            layoutToggleProducts = itemView.findViewById(R.id.layoutToggleProducts);
            tvToggleProducts     = itemView.findViewById(R.id.tvToggleProducts);
            ivToggleProducts     = itemView.findViewById(R.id.ivToggleProducts);
            layoutExtraProducts  = itemView.findViewById(R.id.layoutExtraProducts);

            btnCancel = itemView.findViewById(R.id.btnCancel);
            btnNext = itemView.findViewById(R.id.btnNext);
        }
    }
}
