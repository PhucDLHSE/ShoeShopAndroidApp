package com.example.shoeshop.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.shoeshop.R;
import com.example.shoeshop.activities.AddressActivity;
import com.example.shoeshop.activities.CartActivity;
import com.example.shoeshop.activities.OrderListActivity;
import com.example.shoeshop.activities.UserInfoActivity;
import com.example.shoeshop.utils.SessionManager;

public class UserProfileFragment extends Fragment {

    private TextView textViewUserName;
    private ImageButton btnCart, btnSettings;

    public UserProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        textViewUserName = view.findViewById(R.id.textViewUserName);
        btnCart = view.findViewById(R.id.btnCart);
        btnSettings = view.findViewById(R.id.btnSettings);

        SessionManager sessionManager = new SessionManager(requireContext());
        String userName = sessionManager.getUserName();
        if (userName == null || userName.isEmpty()) userName = "Người dùng";

        textViewUserName.setText("Xin chào, " + userName);

        btnCart.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CartActivity.class);
            startActivity(intent);
        });

        btnSettings.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Đi đến Cài đặt", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(requireContext(), SettingsActivity.class));
        });

        CardView accountInfoSection = view.findViewById(R.id.accountInfoSection);
        accountInfoSection.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), UserInfoActivity.class);
            startActivity(intent);
        });

        LinearLayout layoutAddress = view.findViewById(R.id.layoutAddress);
        layoutAddress.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddressActivity.class);
            startActivity(intent);
        });

        LinearLayout layoutLogout = view.findViewById(R.id.layoutLogout);
        layoutLogout.setOnClickListener(v -> {
            new SessionManager(requireContext()).logout();
        });

        // Đã đặt hàng
        LinearLayout layoutOrdered = view.findViewById(R.id.layoutOrdered);
        layoutOrdered.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), com.example.shoeshop.activities.OrderListActivity.class);
            intent.putExtra("status", "ordered");
            startActivity(intent);
        });

        // Đang thực hiện
        LinearLayout layoutProcessing = view.findViewById(R.id.layoutProcessing);
        layoutProcessing.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), OrderListActivity.class);
            intent.putExtra("status", "processing");
            startActivity(intent);
        });

        //Chờ giao hàng
        LinearLayout layoutWaitingShip = view.findViewById(R.id.layoutWaitingShip);
        layoutWaitingShip.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), OrderListActivity.class);
            intent.putExtra("status", "waiting-ship");
            startActivity(intent);
        });

        //Đang giao hàng
        LinearLayout layoutShipping = view.findViewById(R.id.layoutShipping);
        layoutShipping.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), OrderListActivity.class);
            intent.putExtra("status", "shipping");
            startActivity(intent);
        });

        //Đã hoàn thành
        LinearLayout layoutComplete = view.findViewById(R.id.layoutComplete);
        layoutComplete.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), OrderListActivity.class);
            intent.putExtra("status", "complete");
            startActivity(intent);
        });
    }

}

