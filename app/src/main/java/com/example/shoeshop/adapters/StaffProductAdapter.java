package com.example.shoeshop.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.shoeshop.R;
import com.example.shoeshop.activities.EditProductActivity;
import com.example.shoeshop.models.Product;
import com.example.shoeshop.network.ApiService;
import com.example.shoeshop.network.ApiClient;
import com.example.shoeshop.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class StaffProductAdapter extends RecyclerView.Adapter<StaffProductAdapter.VH> {
    private final List<Product> list = new ArrayList<>();
    private final Context context;
    private final String token;
    private final ApiService api;

    public StaffProductAdapter(Context context, String token) {
        this.context = context;
        this.token = token;
        this.api = ApiClient.getClient().create(ApiService.class);
    }

    public void setData(List<Product> data) {
        list.clear();
        list.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.staff_item_product_card, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int i) {
        Product p = list.get(i);
        h.tvProductId.setText("ID: " + p.getProductID());
        h.tvProductName.setText(p.getProductName());
        h.tvStatus.setText("Status: " + (p.isStatus() ? "Available" : "Out of Stock"));
        Glide.with(context).load(p.getImageUrl()).into(h.ivProductImage);

        // Dữ liệu bên trong phần chi tiết
        h.tvDescription.setText("Desc: " + p.getDescription());
        h.tvSize.setText("Size: " + p.getSize());
        h.tvColor.setText("Color: " + p.getColor());
        h.tvPrice.setText("Price: " + p.getPrice());
        h.tvDiscount.setText("Discount: " + p.getDiscount());
        h.tvTotal.setText("Total: " + p.getTotal());
        h.tvSoldQty.setText("Sold: " + p.getSoldQuantity());
        h.tvStockQty.setText("Stock: " + p.getStockQuantity());
        h.tvIsActive.setText("Active: " + p.isActive());

        // Toggle logic
        h.llDetails.setVisibility(View.GONE); // Đảm bảo accordion ban đầu đóng
        h.tvToggleDetails.setText("Xem thêm");

        h.tvToggleDetails.setOnClickListener(v -> {
            boolean isVisible = h.llDetails.getVisibility() == View.VISIBLE;
            h.llDetails.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            h.tvToggleDetails.setText(isVisible ? "Xem thêm" : "Thu gọn");
        });

        // Edit
        h.btnEditProduct.setOnClickListener(v -> {
            context.startActivity(new Intent(context, EditProductActivity.class)
                    .putExtra("productId", p.getProductID()));
        });
        // Delete
        h.btnDeleteProduct.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Confirm Delete")
                    .setMessage("Delete product " + p.getProductName() + "?")
                    .setPositiveButton("Yes", (dlg, idx) -> {
                        api.deleteProduct("Bearer " + token, p.getProductID())
                                .enqueue(new Callback<Void>(){
                                    @Override public void onResponse(Call<Void> c, Response<Void> r) {
                                        list.remove(i);
                                        notifyItemRemoved(i);
                                    }
                                    @Override public void onFailure(Call<Void> c, Throwable t) {}
                                });
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @Override public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductId, tvProductName, tvStatus, tvToggleDetails;
        LinearLayout llDetails;
        TextView tvDescription, tvSize, tvColor, tvPrice, tvDiscount, tvTotal, tvSoldQty, tvStockQty, tvIsActive;
        Button btnEditProduct, btnDeleteProduct;

        public VH(@NonNull View v) {
            super(v);
            ivProductImage  = v.findViewById(R.id.ivProductImage);
            tvProductId     = v.findViewById(R.id.tvProductId);
            tvProductName   = v.findViewById(R.id.tvProductName);
            tvStatus        = v.findViewById(R.id.tvStatus);
            tvToggleDetails = v.findViewById(R.id.tvToggleDetails);
            llDetails       = v.findViewById(R.id.llDetails);
            tvDescription   = v.findViewById(R.id.tvDescription);
            tvSize          = v.findViewById(R.id.tvSize);
            tvColor         = v.findViewById(R.id.tvColor);
            tvPrice         = v.findViewById(R.id.tvPrice);
            tvDiscount      = v.findViewById(R.id.tvDiscount);
            tvTotal         = v.findViewById(R.id.tvTotal);
            tvSoldQty       = v.findViewById(R.id.tvSoldQty);
            tvStockQty      = v.findViewById(R.id.tvStockQty);
            tvIsActive      = v.findViewById(R.id.tvIsActive);
            btnEditProduct  = v.findViewById(R.id.btnEditProduct);
            btnDeleteProduct= v.findViewById(R.id.btnDeleteProduct);
        }
    }
}