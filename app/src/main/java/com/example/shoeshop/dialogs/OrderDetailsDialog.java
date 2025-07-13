package com.example.shoeshop.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.shoeshop.R;
import com.example.shoeshop.models.Order;
import java.util.List;

public class OrderDetailsDialog extends Dialog {

    public OrderDetailsDialog(@NonNull Context context, List<Order.OrderDetail> details) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_order_details);

        // Set dialog width to match parent with margin
        Window window = getWindow();
        if (window != null) {
            window.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }

        RecyclerView rv = findViewById(R.id.rvOrderDetails);
        rv.setLayoutManager(new LinearLayoutManager(context));
        rv.setAdapter(new DetailsAdapter(details));

        findViewById(R.id.ivClose).setOnClickListener(v -> dismiss());
    }

    private static class DetailsAdapter extends RecyclerView.Adapter<DetailsAdapter.VH> {
        private final List<Order.OrderDetail> list;

        DetailsAdapter(List<Order.OrderDetail> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_detail, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int pos) {
            Order.OrderDetail d = list.get(pos);
            holder.tvOrderDetailID.setText("Mã CTĐH: " + d.getOrderDetailID());
            holder.tvOrderID.setText("Mã ĐH: " + d.getOrderID());
            holder.tvProductID.setText("Mã SP: " + d.getProductID());
            holder.tvProductName.setText("Tên SP: " + d.getProductName());
            holder.tvSizeColor.setText("Size: " + d.getSize() + ", Màu: " + d.getColor());
            holder.tvQuantity.setText("Số lượng: " + d.getQuantity());
            holder.tvPrice.setText("Đơn giá: " + d.getPrice() + " đ");
            holder.tvTotal.setText("Thành tiền: " + d.getTotal() + " đ");
            holder.tvPaymentDate.setText("Ngày thanh toán: " + d.getPaymentDate());
            holder.tvIsActive.setText("Hoạt động: " + (d.isActive() ? "Có" : "Không"));

        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvOrderDetailID, tvOrderID, tvProductID, tvProductName,
                    tvSizeColor, tvQuantity, tvPrice, tvTotal, tvPaymentDate, tvIsActive;
            VH(@NonNull View v) {
                super(v);
                tvOrderDetailID = v.findViewById(R.id.tvOrderDetailID);
                tvOrderID = v.findViewById(R.id.tvOrderID);
                tvProductID = v.findViewById(R.id.tvProductID);
                tvProductName = v.findViewById(R.id.tvProductName);
                tvSizeColor = v.findViewById(R.id.tvSizeColor);
                tvQuantity = v.findViewById(R.id.tvQuantity);
                tvPrice = v.findViewById(R.id.tvPrice);
                tvTotal = v.findViewById(R.id.tvTotal);
                tvPaymentDate = v.findViewById(R.id.tvPaymentDate);
                tvIsActive = v.findViewById(R.id.tvIsActive);
            }
        }
    }
}
