package com.example.shoeshop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.shoeshop.R;
import com.example.shoeshop.models.Product;
import java.util.ArrayList;
import java.util.List;

public class ProductSimpleAdapter extends RecyclerView.Adapter<ProductSimpleAdapter.VH> {
    public interface OnProductClick {
        void onClick(Product product);
    }
    private final List<Product> list = new ArrayList<>();
    private final Context context;
    private final OnProductClick listener;

    public ProductSimpleAdapter(Context context, OnProductClick listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setData(List<Product> data) {
        list.clear();
        list.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_product_simple, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH holder, int position) {
        Product p = list.get(position);
        holder.tvName.setText(p.getProductName());
        holder.itemView.setOnClickListener(v -> listener.onClick(p));
    }

    @Override public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName;
        VH(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tvSimpleProductName);
        }
    }
}
