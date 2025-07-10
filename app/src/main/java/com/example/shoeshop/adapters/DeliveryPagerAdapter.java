package com.example.shoeshop.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import java.util.List;

public class DeliveryPagerAdapter extends FragmentStateAdapter {
    private final List<Fragment> list;
    public DeliveryPagerAdapter(@NonNull Fragment f, List<Fragment> l){
        super(f); this.list = l;
    }
    @NonNull @Override
    public Fragment createFragment(int pos){ return list.get(pos); }
    @Override public int getItemCount(){ return list.size(); }
}