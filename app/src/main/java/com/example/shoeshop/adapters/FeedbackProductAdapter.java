package com.example.shoeshop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeshop.R;
import com.example.shoeshop.models.Feedback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FeedbackProductAdapter extends RecyclerView.Adapter<FeedbackProductAdapter.VH> {

    private final List<Feedback> data = new ArrayList<>();
    private final SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final Context context;

    public FeedbackProductAdapter(Context ctx) {
        this.context = ctx;
    }

    public void setData(List<Feedback> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_feedback, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Feedback f = data.get(position);
        h.tvUserName.setText(f.getName());
        h.ratingBar.setRating(f.getRating());
        h.tvComment.setText(f.getComment());
        if (f.getCreatedAt() != null) {
            h.tvDate.setText(fmt.format(f.getCreatedAt()));
        } else {
            h.tvDate.setText("");
        }
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvUserName, tvComment, tvDate;
        RatingBar ratingBar;
        VH(@NonNull View v) {
            super(v);
            tvUserName = v.findViewById(R.id.tvUserName);
            ratingBar  = v.findViewById(R.id.ratingBar);
            tvComment  = v.findViewById(R.id.tvComment);
            tvDate     = v.findViewById(R.id.tvDate);
        }
    }
}
