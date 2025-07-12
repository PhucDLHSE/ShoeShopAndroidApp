package com.example.shoeshop.adapters;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shoeshop.R;
import com.example.shoeshop.models.Order;
import com.example.shoeshop.models.Order.OrderDetail;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final Context context;
    private final List<Order> orders;
    private final SparseBooleanArray expandedMap = new SparseBooleanArray();   // key = position

    public OrderAdapter(Context context, List<Order> orders) {
        this.context = context;
        this.orders  = orders;
    }

    /* -------------------- ViewHolder -------------------- */
    static class OrderViewHolder extends RecyclerView.ViewHolder {
        ImageView    imgThumb;
        TextView     tvName, tvProductInfo, tvOrderId, tvStatus, tvDate, tvAmount,
                btnToggleProducts;
        LinearLayout layoutExtraProducts;

        OrderViewHolder(@NonNull View v) {
            super(v);
            imgThumb            = v.findViewById(R.id.imgProductThumb);
            tvName              = v.findViewById(R.id.tvProductName);
            tvProductInfo       = v.findViewById(R.id.tvProductInfo);
            tvOrderId           = v.findViewById(R.id.tvOrderId);
            tvStatus            = v.findViewById(R.id.tvStatus);
            tvDate              = v.findViewById(R.id.tvDate);
            tvAmount            = v.findViewById(R.id.tvAmount);
            layoutExtraProducts = v.findViewById(R.id.layoutExtraProducts);
            btnToggleProducts   = v.findViewById(R.id.btnToggleProducts);
        }
    }

    /* -------------------- Adapter overrides -------------------- */
    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_card, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder h, int pos) {
        Order order = orders.get(pos);
        List<OrderDetail> details = order.getOrderDetails();

        if (details != null && !details.isEmpty()) {
            OrderDetail first = details.get(0);
            h.tvName.setText(first.getProductName());

            String info = formatPrice(first.getPrice()) + "  x" + first.getQuantity();
            h.tvProductInfo.setText(info);

            Glide.with(context)
                    .load(first.getImageUrl())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.image_error)
                    .into(h.imgThumb);
        }
        /* ---- Thông tin đơn ---- */
        String idShort = order.getOrderID().length() > 6
                ? order.getOrderID().substring(order.getOrderID().length() - 6)
                : order.getOrderID();
        h.tvOrderId.setText("Mã đơn: #" + idShort);
        h.tvStatus.setText(order.getStatus());
        h.tvDate.setText("Ngày đặt: " + toDDMMYYYY(order.getOrderDate()));
        h.tvAmount.setText(order.getTotalFormatted());

        boolean hasMore    = details != null && details.size() > 1;
        boolean isExpanded = expandedMap.get(pos, false);

        h.btnToggleProducts.setVisibility(hasMore ? View.VISIBLE : View.GONE);
        h.btnToggleProducts.setText(isExpanded ? "Thu gọn ⌃" : "Xem thêm ˅");
        h.layoutExtraProducts.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        h.layoutExtraProducts.removeAllViews();
        if (isExpanded && hasMore) {
            for (int i = 1; i < details.size(); i++) {
                OrderDetail d = details.get(i);

                LinearLayout row = new LinearLayout(context);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(0, 8, 0, 8);
                row.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

                ImageView img = new ImageView(context);
                LinearLayout.LayoutParams lpImg = new LinearLayout.LayoutParams(
                        (int) dp(90), ViewGroup.LayoutParams.WRAP_CONTENT);
                lpImg.setMarginEnd((int) dp(12));
                img.setLayoutParams(lpImg);
                img.setAdjustViewBounds(true);
                img.setScaleType(ImageView.ScaleType.CENTER_CROP);

                Glide.with(context)
                        .load(d.getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(img);

                TextView tv = new TextView(context);
                tv.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                tv.setText(d.getProductName() + "\n" +
                        formatPrice(d.getPrice()) + "  x" + d.getQuantity());
                tv.setTextSize(14);

                row.addView(img);
                row.addView(tv);
                h.layoutExtraProducts.addView(row);
            }
        }

        h.btnToggleProducts.setOnClickListener(v -> {
            expandedMap.put(pos, !expandedMap.get(pos, false));
            notifyItemChanged(pos);
        });
    }

    @Override
    public int getItemCount() {
        return orders == null ? 0 : orders.size();
    }

    /* -------------------- Utils -------------------- */
    private String toDDMMYYYY(String iso) {
        try {
            SimpleDateFormat in  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat out = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date d = in.parse(iso);
            return d != null ? out.format(d) : iso;
        } catch (Exception e) { return iso; }
    }

    private String formatPrice(long price) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(price) + "đ";
    }

    private float dp(float px) {
        return px * context.getResources().getDisplayMetrics().density;
    }
}
