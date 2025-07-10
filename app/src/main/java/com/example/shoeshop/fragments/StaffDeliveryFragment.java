package com.example.shoeshop.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.example.shoeshop.R;
import com.example.shoeshop.activities.CreateDeliveryNoteActivity;
import com.example.shoeshop.adapters.DeliveryPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.*;

public class StaffDeliveryFragment extends Fragment {
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle b) {
        return i.inflate(R.layout.fragment_delivery, c, false);
    }
    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s){
        Button btn = v.findViewById(R.id.btnCreateDeliveryNote);
        btn.setOnClickListener(x-> startActivity(
                new Intent(getContext(), CreateDeliveryNoteActivity.class)
        ));

        TabLayout tab = v.findViewById(R.id.tabDelivery);
        ViewPager2 vp = v.findViewById(R.id.viewPagerDelivery);
        List<Fragment> fr = Arrays.asList(
                new DeliveryStatusFragment("chờ lấy hàng"),
                new DeliveryStatusFragment("đang giao hàng"),
                new DeliveryStatusFragment("đã giao hàng")
        );
        vp.setAdapter(new DeliveryPagerAdapter(this, fr));
        String[] titles = {"Chờ lấy hàng","Đang giao hàng","Đã giao hàng"};
        new TabLayoutMediator(tab,vp,(t,p)->t.setText(titles[p])).attach();
    }
}