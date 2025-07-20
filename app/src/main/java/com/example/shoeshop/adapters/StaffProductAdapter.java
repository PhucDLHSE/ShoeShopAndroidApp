package com.example.shoeshop.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
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

import java.text.NumberFormat;
import java.util.Locale;
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
        final int position = h.getAdapterPosition();
        Product p = list.get(position);
        h.tvProductName.setText(p.getProductName());
        h.tvProductName.setTypeface(null, Typeface.BOLD);
        Glide.with(context).load(p.getImageUrl()).into(h.ivProductImage);

        // Edit
        h.ivEditProduct.setOnClickListener(v -> {
            context.startActivity(new Intent(context, EditProductActivity.class)
                    .putExtra("productId", p.getProductID()));
        });
        // Delete
        h.ivDeleteProduct.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Xác Nhận Xoá?")
                    .setMessage("Xoá Sản Phẩm " + p.getProductName() + "?")
                    .setPositiveButton("Xoá", (dlg, idx) -> {
                        api.deleteProduct("Bearer " + token, p.getProductID())
                                .enqueue(new Callback<Void>(){
                                    @Override public void onResponse(Call<Void> c, Response<Void> r) {
                                        list.remove(position);
                                        notifyItemRemoved(position);
                                    }
                                    @Override public void onFailure(Call<Void> c, Throwable t) {}
                                });
                    })
                    .setNegativeButton("Huỷ", null)
                    .show();
        });
    }

    @Override public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivProductImage, ivEditProduct, ivDeleteProduct;
        TextView  tvProductName;
        TextView  tvPrice, tvTotal, tvIsActive;
        public VH(@NonNull View v) {
            super(v);
            ivProductImage  = v.findViewById(R.id.ivProductImage);
            tvProductName   = v.findViewById(R.id.tvProductName);
            tvPrice         = v.findViewById(R.id.tvPrice);
            tvTotal         = v.findViewById(R.id.tvTotal);
            tvIsActive      = v.findViewById(R.id.tvIsActive);
            ivEditProduct   = v.findViewById(R.id.ivEditProduct);
            ivDeleteProduct = v.findViewById(R.id.ivDeleteProduct);
        }
    }
}