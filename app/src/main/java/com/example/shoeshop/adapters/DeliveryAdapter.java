package com.example.shoeshop.adapters;

import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shoeshop.R;
import com.example.shoeshop.models.DeliveryStatusResponse;
import com.example.shoeshop.models.Order;
import com.example.shoeshop.models.PatchDeliveryResponse;
import com.example.shoeshop.network.ApiService;
import com.example.shoeshop.network.ApiClient;
import com.example.shoeshop.utils.CustomDateAdapter;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DeliveryAdapter extends RecyclerView.Adapter<DeliveryAdapter.VH> {
    private final List<DeliveryStatusResponse> list;
    private final String token, status;
    private final ApiService api;
    private final SparseBooleanArray expandedMap = new SparseBooleanArray();

    private final Map<String, List<Order.OrderDetail>> detailsCache = new HashMap<>(); // Cache chi tiết đơn hàng
    private final Map<String, String> userIdCache = new HashMap<>();

    public DeliveryAdapter(List<DeliveryStatusResponse> data, String token, String status) {
        this.list = new ArrayList<>(data);
        this.token = token;
        this.status = status;
        this.api = ApiClient.getClient().create(ApiService.class);
    }

    public void updateData(List<DeliveryStatusResponse> newList) {
        list.clear();
        list.addAll(newList);
        detailsCache.clear();
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_delivery_card, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        DeliveryStatusResponse d = list.get(pos);
        String orderId = d.getOrderID();

        // 1) Bind các thông tin giao hàng
        h.tvDeliveryId.setText("Mã Vận Chuyển: " + d.getDeliveryID().substring(0, 8));
        h.tvDeliveryStatus.setText(d.getDeliveryStatus());
        h.tvDeliveryAddress.setText("Địa Chỉ: " + d.getDeliveryAddress());
        h.tvShipperName.setText("Shipper: " + d.getShipperName());
        h.tvShipperPhone.setText("SĐT: " + d.getShipperPhone());
        h.tvStorageName.setText("Kho: " + d.getStorageName());
        if (d.getNote() != null && !d.getNote().isEmpty()) {
            h.tvNote.setText("Ghi Chú: " + d.getNote());
        } else {
            h.tvNote.setText("Ghi Chú: ...");
        }
        h.tvOrderId.setText("Mã đơn hàng: " + orderId.substring(0, 8));

        try {
            String rawDate = d.getDeliveryDate();

            if (rawDate != null && rawDate.startsWith("0001-01-01")) {
                h.tvDeliveryDate.setText("Ngày Giao: Không xác định");
            } else {
                // Xử lý định dạng ngày ngay tại chỗ
                String formattedDate = formatDeliveryDate(rawDate);
                h.tvDeliveryDate.setText("Ngày Giao: " + formattedDate);
            }
        } catch (Exception e) {
            h.tvDeliveryDate.setText("Ngày Giao: Không xác định");
            Log.e("DeliveryDate", "Lỗi định dạng ngày", e);
        }



        // 2) Ẩn/hiện nút Next/Cancel theo status
        h.btnNext.setVisibility(View.GONE);
        h.btnCancel.setVisibility(View.GONE);
        if ("chờ lấy hàng".equals(status)) {
            h.btnNext.setText("Đang giao hàng");
            h.btnNext.setVisibility(View.VISIBLE);
            h.btnNext.setOnClickListener(v ->
                    api.startDelivery("Bearer "+token, d.getDeliveryID()).enqueue(callbackRemove(pos))
            );
            h.btnCancel.setText("Hủy");
            h.btnCancel.setVisibility(View.VISIBLE);
            h.btnCancel.setOnClickListener(v ->
                    api.cancelDelivery("Bearer "+token, d.getDeliveryID()).enqueue(callbackRemove(pos))
            );
        } else if ("đang giao hàng".equals(status)) {
            h.btnNext.setText("Hoàn thành");
            h.btnNext.setVisibility(View.VISIBLE);
            h.btnNext.setOnClickListener(v ->
                    api.completeDelivery("Bearer "+token, d.getDeliveryID()).enqueue(callbackRemove(pos))
            );
        }

        // 3) Lấy chi tiết đơn hàng (orderDetails) nếu chưa cache
        List<Order.OrderDetail> details = detailsCache.get(orderId);
        String userId = userIdCache.get(orderId);
        if (details != null) {
            bindFirstProduct(h, details.get(0));
            bindExtraProducts(h, pos, details);
            if (userId != null && !userId.isEmpty()) {
                h.tvUserId.setText("Mã Khách Hàng: " + userId.substring(0, 8));
            }
        } else {
            api.getOrderById("Bearer " + token, orderId)
                    .enqueue(new Callback<Order>() {
                        @Override public void onResponse(Call<Order> call, Response<Order> resp) {
                            if (resp.isSuccessful() && resp.body() != null) {
                                List<Order.OrderDetail> od = resp.body().getOrderDetails();
                                String uid = resp.body().getUserID();
                                detailsCache.put(orderId, od);
                                userIdCache.put(orderId, uid);
                                bindFirstProduct(h, od.get(0));
                                bindExtraProducts(h, pos, od);
                                h.tvUserId.setText("Mã Khách Hàng: " + resp.body().getUserID().substring(0, 8));
                            }
                        }
                        @Override public void onFailure(Call<Order> call, Throwable t) { }
                    });
        }
    }

    private void bindFirstProduct(VH h, Order.OrderDetail d0) {
        h.ivProductThumb.setVisibility(View.VISIBLE);
        Glide.with(h.ivProductThumb.getContext())
                .load(d0.getImageUrl())
                .placeholder(R.drawable.placeholder)
                .into(h.ivProductThumb);
        h.tvProductName.setText(d0.getProductName());
        String info = NumberFormat.getInstance(new Locale("vi","VN")).format(d0.getPrice())
                + " x" + d0.getQuantity();
        h.tvProductInfo.setText(info);
    }

    private void bindExtraProducts(VH h, int pos, List<Order.OrderDetail> details) {
        boolean hasMore = details.size() > 1;
        boolean isExp = expandedMap.get(pos, false);
        h.layoutToggleProducts.setVisibility(hasMore ? View.VISIBLE : View.GONE);
        h.tvToggleProducts.setText(isExp ? "Thu gọn" : "Xem thêm");
        h.ivToggleProducts.setRotation(isExp ? 180 : 0);
        h.layoutExtraProducts.setVisibility(isExp ? View.VISIBLE : View.GONE);

        if (isExp && hasMore) {
            h.layoutExtraProducts.removeAllViews();
            for (int i = 1; i < details.size(); i++) {
                Order.OrderDetail od = details.get(i);
                // Tạo row container
                LinearLayout row = new LinearLayout(h.itemView.getContext());
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(0,8,0,8);

                ImageView img = new ImageView(h.itemView.getContext());
                int px = (int)(90 * h.itemView.getResources().getDisplayMetrics().density);
                row.addView(img, new LinearLayout.LayoutParams(px, ViewGroup.LayoutParams.WRAP_CONTENT));
                img.setAdjustViewBounds(true);
                img.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Glide.with(img.getContext()).load(od.getImageUrl())
                        .placeholder(R.drawable.placeholder).into(img);

                LinearLayout textLay = new LinearLayout(h.itemView.getContext());
                textLay.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.setMargins((int)(14 * h.itemView.getResources().getDisplayMetrics().density), 0, 0, 0);
                textLay.setLayoutParams(lp);

                TextView tName = new TextView(h.itemView.getContext());
                tName.setText(od.getProductName());
                tName.setTextSize(14);

                TextView tInfo = new TextView(h.itemView.getContext());
                tInfo.setText(
                        NumberFormat.getInstance(new Locale("vi","VN")).format(od.getPrice())
                                + " x" + od.getQuantity()
                );
                tInfo.setTextSize(13);
                tInfo.setTextColor(Color.GRAY);

                textLay.addView(tName);
                textLay.addView(tInfo);
                row.addView(textLay);

                h.layoutExtraProducts.addView(row);
            }
        }

        h.layoutToggleProducts.setOnClickListener(v -> {
            boolean expanded = expandedMap.get(pos, false);
            expandedMap.put(pos, !expanded);
            notifyItemChanged(pos);
        });
    }

    private Callback<PatchDeliveryResponse> callbackRemove(int pos) {
        return new Callback<PatchDeliveryResponse>() {
            @Override public void onResponse(Call<PatchDeliveryResponse> c, Response<PatchDeliveryResponse> r) {
                if (r.isSuccessful()) { list.remove(pos); notifyItemRemoved(pos); }
            }
            @Override public void onFailure(Call<PatchDeliveryResponse> c, Throwable t) {}
        };
    }

    @Override public int getItemCount() { return list.size(); }

    private String formatDeliveryDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) return "Không xác định";

        try {
            // Loại bỏ 'Z' nếu có
            if (rawDate.endsWith("Z")) {
                rawDate = rawDate.substring(0, rawDate.length() - 1);
            }

            // Cắt bớt phần thập phân giây (chỉ giữ lại 3 chữ số)
            int dotIndex = rawDate.indexOf('.');
            if (dotIndex != -1) {
                int endIndex = Math.min(dotIndex + 4, rawDate.length());
                rawDate = rawDate.substring(0, endIndex);
            }

            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

            Date date = inputFormat.parse(rawDate);
            return outputFormat.format(date);

        } catch (Exception e) {
            Log.e("DateParse", "Lỗi parse ngày: " + rawDate, e);
            return "Không xác định";
        }
    }


    static class VH extends RecyclerView.ViewHolder {
        TextView tvDeliveryId, tvDeliveryStatus, tvDeliveryAddress,
                tvDeliveryDate, tvShipperName, tvShipperPhone,
                tvStorageName, tvNote, tvIsActive,
                tvToggleProducts, tvProductName, tvProductInfo, tvOrderId, tvUserId;
        ImageView ivProductThumb, ivToggleProducts;
        LinearLayout layoutExtraProducts, layoutToggleProducts;
        Button btnNext, btnCancel;

        VH(@NonNull View v) {
            super(v);
            tvDeliveryId = v.findViewById(R.id.tvDeliveryId);
            tvDeliveryStatus = v.findViewById(R.id.tvDeliveryStatus);
            tvDeliveryAddress = v.findViewById(R.id.tvDeliveryAddress);
            tvDeliveryDate = v.findViewById(R.id.tvDeliveryDate);
            tvShipperName = v.findViewById(R.id.tvShipperName);
            tvShipperPhone = v.findViewById(R.id.tvShipperPhone);
            tvStorageName = v.findViewById(R.id.tvStorageName);
            tvNote = v.findViewById(R.id.tvNote);
            tvIsActive = v.findViewById(R.id.tvIsActive);
            tvProductName = v.findViewById(R.id.tvProductName);
            tvProductInfo = v.findViewById(R.id.tvProductInfo);
            tvToggleProducts = v.findViewById(R.id.tvToggleProducts);
            tvOrderId = v.findViewById(R.id.tvOrderId);
            tvUserId = v.findViewById(R.id.tvUserId);

            layoutExtraProducts = v.findViewById(R.id.layoutExtraProducts);
            layoutToggleProducts = v.findViewById(R.id.layoutToggleProducts);

            btnNext = v.findViewById(R.id.btnNext);
            btnCancel = v.findViewById(R.id.btnCancel);

            ivProductThumb = v.findViewById(R.id.ivProductThumb);
            ivToggleProducts = v.findViewById(R.id.ivToggleProducts);
        }
    }
}