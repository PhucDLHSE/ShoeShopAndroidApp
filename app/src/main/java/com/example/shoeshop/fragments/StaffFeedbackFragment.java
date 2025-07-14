package com.example.shoeshop.fragments;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.example.shoeshop.R;
import com.example.shoeshop.adapters.FeedbackPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.*;

public class StaffFeedbackFragment extends Fragment {
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_staff_feedback, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        TabLayout tab = v.findViewById(R.id.tabFeedback);
        ViewPager2 vp  = v.findViewById(R.id.viewPagerFeedback);
        List<Fragment> frags = Arrays.asList(
                new FeedbackAllFragment(),
                new FeedbackProductFragment()
        );
        vp.setAdapter(new FeedbackPagerAdapter(this, frags));
        String[] titles = {"All","By Product"};
        new TabLayoutMediator(tab, vp, (t,pos)->t.setText(titles[pos])).attach();
    }
}