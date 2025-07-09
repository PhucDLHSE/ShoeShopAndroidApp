package com.example.shoeshop.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeshop.R;
import com.example.shoeshop.models.CustomerAddress;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private final List<CustomerAddress> addressList;
    private final OnAddressActionListener listener;

    public interface OnAddressActionListener {
        void onSetDefault(String addressId);
    }

    public AddressAdapter(List<CustomerAddress> addressList, OnAddressActionListener listener) {
        this.addressList = addressList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        CustomerAddress address = addressList.get(position);

        holder.tvFullAddress.setText(address.getFullAddress());

        String detail = String.format("%s, %s, %s, %s",
                address.getStreetAddress(),
                address.getWard(),
                address.getDistrict(),
                address.getCity());
        holder.tvDetail.setText(detail);

        String contact = String.format("%s - %s",
                address.getContactName(),
                address.getContactPhone());
        holder.tvContact.setText(contact);

        // Hiển thị “Mặc định” hoặc nút “Đặt làm mặc định”
        if (address.isDefault()) {
            holder.tvDefault.setVisibility(View.VISIBLE);
            holder.btnSetDefault.setVisibility(View.GONE);
        } else {
            holder.tvDefault.setVisibility(View.GONE);
            holder.btnSetDefault.setVisibility(View.VISIBLE);
            holder.btnSetDefault.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSetDefault(address.getAddressID());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return addressList != null ? addressList.size() : 0;
    }

    public static class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView tvFullAddress, tvDetail, tvContact, tvDefault;
        Button btnSetDefault;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFullAddress = itemView.findViewById(R.id.tvFullAddress);
            tvDetail = itemView.findViewById(R.id.tvDetail);
            tvContact = itemView.findViewById(R.id.tvContact);
            tvDefault = itemView.findViewById(R.id.tvDefault);
            btnSetDefault = itemView.findViewById(R.id.btnSetDefault);
        }
    }
}
