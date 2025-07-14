package com.example.shoeshop.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.shoeshop.R;
import com.example.shoeshop.adapters.FeedbackAdapter;
import com.example.shoeshop.models.Feedback;
import com.example.shoeshop.network.ApiClient;
import com.example.shoeshop.network.ApiService;
import com.example.shoeshop.network.FeedbackApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.*;

public class FeedbackAllFragment extends Fragment {
    private RecyclerView rv;
    private ProgressBar pb;
    private FeedbackAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup c, Bundle b) {
        return inflater.inflate(R.layout.fragment_feedback_all, c, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        swipeRefresh = v.findViewById(R.id.swipeRefreshAllFeedback);
        rv = v.findViewById(R.id.rvAllFeedback);
        pb = v.findViewById(R.id.pbFeedbackAll);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FeedbackAdapter(new ArrayList<>(), getContext());
        rv.setAdapter(adapter);
        swipeRefresh.setOnRefreshListener(this::loadAll);
        loadAll();
    }

    private void loadAll() {
        Log.d("FeedbackAllFragment", "loadAll() called");
        swipeRefresh.setRefreshing(true);
        pb.setVisibility(View.VISIBLE);
        ApiService api = FeedbackApiClient.getClient().create(ApiService.class);
        api.getAllFeedbacks().enqueue(new Callback<List<Feedback>>() {
            @Override public void onResponse(Call<List<Feedback>> c, Response<List<Feedback>> r) {
                swipeRefresh.setRefreshing(false);
                pb.setVisibility(View.GONE);
                Log.d("FeedbackAllFragment", "onResponse called");
                if (r.isSuccessful() && r.body() != null) {
                    List<Feedback> feedbacks = r.body();
                    Log.d("FeedbackAllFragment", "Feedback count: " + feedbacks.size());
                    adapter.updateData(feedbacks);
                }else {
                    Log.d("FeedbackAllFragment", "Response not successful or body is null");
                }
            }
            @Override public void onFailure(Call<List<Feedback>> c, Throwable t) {
                swipeRefresh.setRefreshing(false);
                pb.setVisibility(View.GONE);
                Log.e("FeedbackAllFragment", "API call failed: ", t);
                Toast.makeText(getContext(), t.getMessage() != null ? t.getMessage() : "Unknown error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}