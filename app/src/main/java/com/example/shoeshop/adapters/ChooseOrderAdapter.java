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
                .inflate(R.layout.item_order_card, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Lấy dữ liệu và bind vào view
        Order order = orders.get(position);
        holder.tvOrderId.setText("Đơn #" + order.getOrderID());

        // Highlight if selected
        holder.itemView.setBackgroundColor(
                holder.getBindingAdapterPosition() == selectedPosition
                        ? Color.LTGRAY
                        : Color.WHITE
        );

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            // Kiểm tra nếu vị trí hợp lệ
            if (pos == RecyclerView.NO_POSITION) return;

            // Cập nhật selectedPosition
            int previous = selectedPosition;
            selectedPosition = pos;

            // Thông báo thay đổi 2 vị trí để UI refresh
            notifyItemChanged(previous);
            notifyItemChanged(pos);

            // Truyền orderId ra ngoài
            onSelect.accept(orders.get(pos).getOrderID());
        });
    }


    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
        }
    }
}