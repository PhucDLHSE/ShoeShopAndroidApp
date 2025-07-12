package com.example.shoeshop.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.shoeshop.R;
import com.example.shoeshop.activities.AddressActivity;
import com.example.shoeshop.activities.CartActivity;
import com.example.shoeshop.activities.MainActivity;
import com.example.shoeshop.activities.OrderListActivity;
import com.example.shoeshop.activities.SettingsActivity;
import com.example.shoeshop.activities.UserInfoActivity;
import com.example.shoeshop.utils.SessionManager;
import com.example.shoeshop.utils.ThemeHelper;

public class UserProfileFragment extends Fragment {

    private TextView textViewUserName;
    private ImageButton btnCart, btnSettings;

    public UserProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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

        btnCart.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), CartActivity.class)));

        btnSettings.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).openSettingsFromTab("profile");
        });

        view.findViewById(R.id.accountInfoSection).setOnClickListener(v ->
                startActivity(new Intent(getContext(), UserInfoActivity.class)));

        view.findViewById(R.id.layoutAddress).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), AddressActivity.class)));

        view.findViewById(R.id.layoutLogout).setOnClickListener(v ->
                new SessionManager(requireContext()).logout());

        setupOrderSection(view, R.id.layoutOrdered, "ordered");
        setupOrderSection(view, R.id.layoutProcessing, "processing");
        setupOrderSection(view, R.id.layoutWaitingShip, "waiting-ship");
        setupOrderSection(view, R.id.layoutShipping, "shipping");
        setupOrderSection(view, R.id.layoutComplete, "complete");
    }

    private void setupOrderSection(View view, int layoutId, String status) {
        view.findViewById(layoutId).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), OrderListActivity.class);
            intent.putExtra("status", status);
            startActivity(intent);
        });
    }
}
