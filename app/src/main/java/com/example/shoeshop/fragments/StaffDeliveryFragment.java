package com.example.shoeshop.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;
import com.example.shoeshop.R;
import com.example.shoeshop.adapters.DeliveryPagerAdapter;
import com.example.shoeshop.dialogs.CreateDeliveryDialogFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.*;

public class StaffDeliveryFragment extends Fragment {
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar pbSectionLoading;
    private List<Fragment> fragments;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_delivery, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        swipeRefresh      = v.findViewById(R.id.swipeRefreshDelivery);
        pbSectionLoading  = v.findViewById(R.id.pbDeliveryLoading);

        Button btn = v.findViewById(R.id.btnCreateDeliveryNote);
        btn.setOnClickListener(x ->
                new CreateDeliveryDialogFragment().show(getParentFragmentManager(), "create_delivery")
        );

        TabLayout tab = v.findViewById(R.id.tabDelivery);
        ViewPager2 vp = v.findViewById(R.id.viewPagerDelivery);

        fragments = new ArrayList<>();
        fragments.add(new DeliveryStatusFragment("chờ lấy hàng"));
        fragments.add(new DeliveryStatusFragment("đang giao hàng"));
        fragments.add(new DeliveryStatusFragment("đã giao hàng"));

        vp.setAdapter(new DeliveryPagerAdapter(this, fragments));
        String[] titles = {"Chờ lấy hàng","Đang giao hàng","Đã giao hàng"};
        int[] tabIcons = {R.drawable.ic_local_shipping, R.drawable.ic_delivery_truck_speed, R.drawable.ic_check };
        new TabLayoutMediator(tab, vp, (t,pos)->t.setIcon(tabIcons[pos]).setText(titles[pos])).attach();

        // Pull-to-refresh section
        swipeRefresh.setOnRefreshListener(() -> {
            pbSectionLoading.setVisibility(View.VISIBLE);
            // Gọi reloadData() cho từng fragment con
            for (Fragment f : fragments) {
                if (f instanceof DeliveryStatusFragment) {
                    ((DeliveryStatusFragment) f).reloadData();
                }
            }
            swipeRefresh.setRefreshing(false);
            pbSectionLoading.setVisibility(View.GONE);
        });
    }
}