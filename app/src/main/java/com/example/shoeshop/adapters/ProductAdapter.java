package com.example.shoeshop.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shoeshop.R;
import com.example.shoeshop.models.Product;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.textViewName.setText(product.getProductName());
        Log.d("ProductAdapter", "Product: " + product.getProductName());
        Log.d("ProductAdapter", "Image URL: " + product.getImageUrl());
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        String priceFormatted = formatter.format(product.getPrice()) + "đ";
        holder.textViewPrice.setText("Giá: " + priceFormatted);

        Glide.with(context)
                .load(product.getImageUrl())
                .placeholder(R.drawable.placeholder)
                .into(holder.imageViewProduct);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, com.example.shoeshop.activities.ProductDetailActivity.class);
            intent.putExtra("productId", product.getProductID());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewProduct;
        TextView textViewName, textViewPrice;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
        }
    }
}
