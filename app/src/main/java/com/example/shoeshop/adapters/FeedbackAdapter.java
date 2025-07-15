package com.example.shoeshop.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.shoeshop.R;
import com.example.shoeshop.models.Feedback;
import com.example.shoeshop.network.ApiClient;
import com.example.shoeshop.network.ApiService;
import com.example.shoeshop.network.FeedbackApiClient;
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
    private final SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public FeedbackAdapter( Context ctx) {
        this.internalList = new ArrayList<>();
        context = ctx;
        api = FeedbackApiClient.getClient().create(ApiService.class);
    }
    public FeedbackAdapter(List<Feedback> initialData, Context context) {
        // QUAN TRỌNG: Tạo một bản sao để tránh vấn đề tham chiếu nếu initialData được sửa đổi bên ngoài
        this.internalList = new ArrayList<>(initialData); // Sao chép dữ liệu ban đầu
        this.context = context;
        // ... khởi tạo các thành phần khác như ApiService
    }

    public void updateData(List<Feedback> newListFromServer) {
        Log.d("FEEDBACK_ADAPTER", "updateData called. New list size from server: " + (newListFromServer != null ? newListFromServer.size() : "null"));
        this.internalList.clear(); // Luôn xóa dữ liệu cũ trước khi thêm mới
        if (newListFromServer != null && !newListFromServer.isEmpty()) {
            this.internalList.addAll(newListFromServer); // Thêm tất cả dữ liệu mới
            Log.d("FEEDBACK_ADAPTER", "Internal list updated. New size: " + this.internalList.size());
        } else {
            Log.d("FEEDBACK_ADAPTER", "New list from server is null or empty. Internal list cleared.");
        }
        notifyDataSetChanged(); // Thông báo cho RecyclerView cập nhật lại giao diện
        // Cân nhắc dùng DiffUtil để hiệu năng tốt hơn với danh sách lớn
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        Log.d("FeedbackAdapter", "onCreateViewHolder called");
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_feedback_card, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Feedback f = internalList.get(position);
        Log.d("FeedbackAdapter", "onBindViewHolder for position: " + position + ", Feedback ID: " + f.getFeedbackID());
        h.tvUserName.setText("Khách Hàng: "+f.getName());
        h.tvProductName.setText("Sản Phẩm: "+f.getProductName());
        h.tvRating.setRating(f.getRating());
        h.tvComment.setText("Nhận Xét: "+f.getComment());
        h.tvDate.setText("Ngày Tạo: "+fmt.format(f.getCreatedAt()));

        h.btnDeleteFeedback.setText("Xoá");
        h.btnDeleteFeedback.setOnClickListener(v -> {
            int pos = h.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            Log.d("FeedbackAdapter", "Delete button clicked for feedback ID: " + f.getFeedbackID());
            new AlertDialog.Builder(context)
                    .setTitle("Xác nhận xoá")
                    .setMessage("Xoá phản hồi này?")
                    .setPositiveButton("Có", (d, w) -> {
                        Log.d("FeedbackAdapter", "Confirm delete for feedback ID: " + f.getFeedbackID());
                        api.deleteFeedback(
                                "Bearer " + new SessionManager(context).getToken(), f.getFeedbackID()
                        ).enqueue(new Callback<Void>() {
                            @Override public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    Log.d("FeedbackAdapter", "Successfully deleted feedback ID: " + f.getFeedbackID());
                                    int currentPos = h.getBindingAdapterPosition();
                                    if (currentPos != RecyclerView.NO_POSITION) {
                                        internalList.remove(currentPos);
                                        notifyItemRemoved(currentPos);
                                        Log.d("FeedbackAdapter", "Removed item from list and notified adapter at position: " + currentPos);
                                    }
                                } else {
                                    Log.e("FeedbackAdapter", "Failed to delete feedback. Response code: " + response.code() + ", Feedback ID: " + f.getFeedbackID());
                                }
                            }
                            @Override public void onFailure(Call<Void> call, Throwable t) {
                                Log.e("FeedbackAdapter", "API call failed for deleteFeedback. Feedback ID: " + f.getFeedbackID(), t);
                            }
                        });
                    })
                    .setNegativeButton("Không", null)
                    .show();
        });
    }

    @Override public int getItemCount() {
        Log.d("FEEDBACK_ADAPTER", "getItemCount called, returning: " + internalList.size());
        return internalList.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvUserName, tvProductName, tvComment, tvDate;
        Button btnDeleteFeedback;
        RatingBar tvRating;
        VH(@NonNull View v) {
            super(v);
            tvUserName = v.findViewById(R.id.tvUserName);
            tvProductName = v.findViewById(R.id.tvProductName);
            tvRating = v.findViewById(R.id.tvRating);
            tvComment = v.findViewById(R.id.tvComment);
            tvDate = v.findViewById(R.id.tvDate);
            btnDeleteFeedback = v.findViewById(R.id.btnDeleteFeedback);
        }
    }

}
