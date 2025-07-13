package com.example.shoeshop.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.shoeshop.R;
import com.example.shoeshop.models.Order;
import java.util.List;
import java.util.function.Consumer;

//Choose Order in Delivery Adapter
public class ChooseOrderAdapter extends RecyclerView.Adapter<ChooseOrderAdapter.ViewHolder> {
    private final List<Order> orders;
    private final Consumer<String> onSelect;
    private int selectedPosition = -1;

    public ChooseOrderAdapter(List<Order> orders, Consumer<String> onSelect) {
        this.orders = orders;
        this.onSelect = onSelect;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_choose_delivery_order_card, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);
        // Bind data
        holder.tvOrderId.setText("Đơn #" + order.getOrderID());
        holder.tvUserId.setText("Người dùng: " + order.getUserID());
        holder.tvDate.setText(order.getOrderDate());
        holder.tvStatus.setText(order.getStatus());
        holder.tvPrice.setText(order.getTotalAmount() + " đ");
        holder.tvAddress.setText(order.getDeliveryAddress());
        holder.tvPaymentMethod.setText(order.getMethodName());
        holder.tvIsActive.setText(order.isActive() ? "Hoạt động" : "Không hoạt động");

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
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvUserId, tvDate, tvStatus,
                tvPrice, tvAddress, tvPaymentMethod, tvIsActive;

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
        }
    }
}
