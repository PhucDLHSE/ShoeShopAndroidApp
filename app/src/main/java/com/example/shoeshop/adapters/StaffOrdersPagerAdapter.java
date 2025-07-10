package com.example.shoeshop.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class StaffOrdersPagerAdapter extends FragmentStateAdapter {
    private final List<Fragment> fragments;

    public StaffOrdersPagerAdapter(@NonNull Fragment fragment, List<Fragment> frags) {
        super(fragment);
        this.fragments = frags;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }
}