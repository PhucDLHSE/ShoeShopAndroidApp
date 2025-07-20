package com.example.shoeshop.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.icu.text.DateFormat;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shoeshop.R;
import com.example.shoeshop.models.Feedback;
import com.example.shoeshop.models.Product;
import com.example.shoeshop.network.ApiClient;
import com.example.shoeshop.network.ApiService;
import com.example.shoeshop.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.VH> {
    private final List<Feedback> internalList;
    private final Context context;
    private ApiService api;
    private final SimpleDateFormat inputFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
    private final SimpleDateFormat outputFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public FeedbackAdapter( Context ctx) {
        this.internalList = new ArrayList<>();
        context = ctx;
        api = ApiClient.getClient().create(ApiService.class);
    }
    public FeedbackAdapter(List<Feedback> initialData, Context context) {
        this.internalList = new ArrayList<>(initialData);
        this.context = context;
        this.api = ApiClient.getClient().create(ApiService.class);
    }

    public void updateData(List<Feedback> newListFromServer) {
        this.internalList.clear();
        if (newListFromServer != null && !newListFromServer.isEmpty()) {
            this.internalList.addAll(newListFromServer);
        } else {
            Log.e("FeedbackAdapter", "newListFromServer is null or empty. Cannot update data.");
        }
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_feedback_card, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Feedback f = internalList.get(position);
        h.tvUserName.setText(f.getName());
        if (f.getProductName() != null) {
            h.tvProductName.setText(f.getProductName());
        } else {
            h.tvProductName.setText("Sản phẩm");
        }
        h.tvRating.setRating(f.getRating());
        if (f.getComment() != null) {
            h.tvComment.setText(f.getComment());
        } else {
            h.tvComment.setText("Nhận Xét");
        }
        if (f.getCreatedAt() != null) {
            try {
                String createdAt = String.valueOf(f.getCreatedAt());

                // Loại bỏ ký tự Z nếu có
                if (createdAt.endsWith("Z")) {
                    createdAt = createdAt.substring(0, createdAt.length() - 1);
                }

                // Tìm dấu chấm phân tách phần milli giây
                int dotIndex = createdAt.indexOf('.');
                if (dotIndex != -1) {
                    // Cắt bớt phần thập phân chỉ lấy 3 chữ số
                    int endIndex = dotIndex + 4;
                    while (endIndex < createdAt.length() && Character.isDigit(createdAt.charAt(endIndex))) {
                        endIndex++;
                    }
                    createdAt = createdAt.substring(0, dotIndex + 4) + createdAt.substring(endIndex); // giữ nguyên phần sau milli giây (nếu có)
                }

                // Parse
                Date date = inputFormatter.parse(createdAt);
                if (date != null) {
                    h.tvDate.setText(outputFormatter.format(date));
                } else {
                    h.tvDate.setText("Ngày không hợp lệ");
                }
            } catch (Exception e) {
                h.tvDate.setText("Ngày không hợp lệ");
                Log.e("FeedbackAdapter", "Date parsing error: " + f.getCreatedAt(), e);
            }
        } else {
            h.tvDate.setText("01/01/1000");
        }

        String productId = f.getProductID();

        api.getProductById(productId)
                .enqueue(new Callback<Product>() {
                    @Override
                    public void onResponse(Call<Product> call, Response<Product> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String imageUrl = response.body().getImageUrl();
                            Glide.with(context)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_image)
                                    .error(R.drawable.image_error)
                                    .into(h.ivProductFeedback);
                        } else {
                            h.ivProductFeedback.setImageResource(R.drawable.image_error);
                        }
                    }
                    @Override
                    public void onFailure(Call<Product> call, Throwable t) {
                        Log.e("FeedbackAdapter", "Lỗi load product image", t);
                        h.ivProductFeedback.setImageResource(R.drawable.image_error);
                    }
                });

        h.btnDeleteFeedback.setOnClickListener(v -> {
            int pos = h.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            new AlertDialog.Builder(context)
                    .setTitle("Xác nhận xoá")
                    .setMessage("Xoá phản hồi này?")
                    .setPositiveButton("Có", (d, w) -> {
                        api.deleteFeedback(
                                "Bearer " + new SessionManager(context).getToken(), f.getFeedbackID()
                        ).enqueue(new Callback<Void>() {
                            @Override public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    int currentPos = h.getBindingAdapterPosition();
                                    if (currentPos != RecyclerView.NO_POSITION) {
                                        internalList.remove(currentPos);
                                        notifyItemRemoved(currentPos);
                                    }
                                } else {
                                    Toast.makeText(context, "Lỗi khi xóa phản hồi", Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override public void onFailure(Call<Void> call, Throwable t) {
                                Log.e("FeedbackAdapter", "Lỗi API khi xóa phản hồi Feedback ID: " + f.getFeedbackID(), t);
                                Toast.makeText(context, "Lỗi khi xóa phản hồi", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Không", null)
                    .show();
        });
    }

    @Override public int getItemCount() {
        return internalList.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvUserName, tvProductName, tvComment, tvDate;
        ImageButton btnDeleteFeedback;
        ImageView ivProductFeedback;
        RatingBar tvRating;
        VH(@NonNull View v) {
            super(v);
            tvUserName = v.findViewById(R.id.tvUserName);
            tvProductName = v.findViewById(R.id.tvProductName);
            tvRating = v.findViewById(R.id.tvRating);
            tvComment = v.findViewById(R.id.tvComment);
            tvDate = v.findViewById(R.id.tvDate);
            btnDeleteFeedback = v.findViewById(R.id.btnDeleteFeedback);
            ivProductFeedback = v.findViewById(R.id.ivProductFeedback);
        }
    }

}
