package com.example.shoeshop.fragments;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;
import com.example.shoeshop.R;
import com.example.shoeshop.adapters.StaffOrdersPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.*;

public class StaffOrdersFragment extends Fragment {
    private SwipeRefreshLayout swipeRefresh;
    private List<OrdersStatusFragment> frags;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orders, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        swipeRefresh = v.findViewById(R.id.swipeRefreshOrders);

        TabLayout tab = v.findViewById(R.id.tabOrders);
        ViewPager2 vp = v.findViewById(R.id.viewPagerOrders);
        frags = Arrays.asList(
                new OrdersStatusFragment("ordered"),
                new OrdersStatusFragment("processing"),
                new OrdersStatusFragment("waiting-ship"),
                new OrdersStatusFragment("shipping"),
                new OrdersStatusFragment("completed"),
                new OrdersStatusFragment("cancled")
        );
        vp.setAdapter(new StaffOrdersPagerAdapter(this, new ArrayList<>(frags)));
        String[] titles = {"Đã đặt","Đang thực hiện","Chờ vận chuyển","Đang giao","Đã giao","Đã huỷ"};
        new TabLayoutMediator(tab, vp, (t,pos) -> t.setText(titles[pos])).attach();

        // Pull-to-refresh: gọi lại loadData() trên từng fragment con
        swipeRefresh.setOnRefreshListener(() -> {
            for (OrdersStatusFragment f : frags) {
                f.reloadData();
            }
            swipeRefresh.setRefreshing(false);
        });
    }
}