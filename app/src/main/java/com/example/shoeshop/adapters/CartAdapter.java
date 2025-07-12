package com.example.shoeshop.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shoeshop.R;
import com.example.shoeshop.models.CartItem;
import com.example.shoeshop.utils.CartStorage;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final Context context;
    private final List<CartItem> cartItems;

    public CartAdapter(Context context, List<CartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        holder.tvName.setText(item.getProductName());
        holder.tvQuantity.setText("Số lượng: " + item.getQuantity());
        String price = NumberFormat.getInstance(new Locale("vi", "VN"))
                .format(item.getPrice()) + "đ";
        holder.tvPrice.setText("Giá: " + price);

        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.placeholder)
                .into(holder.imgCartItem);

        // ------ CheckBox ------
        holder.checkbox.setOnCheckedChangeListener(null);
        holder.checkbox.setChecked(item.isSelected());
        holder.checkbox.setOnCheckedChangeListener((btn, checked) ->
                item.setSelected(checked)
        );

        // ------ nút Xoá ------
        holder.btnRemove.setOnClickListener(v -> {
            int adapterPos = holder.getAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) return;

            new AlertDialog.Builder(context)
                    .setTitle("Xác nhận xoá")
                    .setMessage("Bạn có chắc muốn xoá sản phẩm này khỏi giỏ hàng?")
                    .setPositiveButton("Xoá", (dialog, which) -> {
                        cartItems.remove(adapterPos);
                        notifyItemRemoved(adapterPos);
                        notifyItemRangeChanged(adapterPos, cartItems.size());
                        CartStorage.saveCart(context, cartItems);
                    })
                    .setNegativeButton("Huỷ", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return cartItems == null ? 0 : cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvQuantity;
        ImageView imgCartItem;
        ImageButton btnRemove;
        CheckBox checkbox;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName     = itemView.findViewById(R.id.tvCartItemName);
            tvPrice    = itemView.findViewById(R.id.tvCartItemPrice);
            tvQuantity = itemView.findViewById(R.id.tvCartItemQuantity);
            imgCartItem= itemView.findViewById(R.id.imgCartItem);
            btnRemove  = itemView.findViewById(R.id.btnRemoveItem);
            checkbox   = itemView.findViewById(R.id.checkboxSelect);
        }
    }
}
