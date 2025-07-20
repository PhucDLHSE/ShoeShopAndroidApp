package com.example.shoeshop.fragments;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.shoeshop.R;
import com.example.shoeshop.utils.SessionManager;

public class StaffProfileFragment extends Fragment {

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate layout giống activity trước
        return inflater.inflate(R.layout.activity_staff_profile, container, false);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind views
        TextView tvName       = view.findViewById(R.id.tvName);
        TextView tvEmail      = view.findViewById(R.id.tvEmail);
        TextView tvPhone      = view.findViewById(R.id.tvPhone);
        TextView tvRole       = view.findViewById(R.id.tvRole);
        Button btnLogout      = view.findViewById(R.id.btnLogout);

        // Load session data
        SessionManager sessionManager = new SessionManager(requireContext());
        String userName  = sessionManager.getUserName();
        String userEmail = sessionManager.getUserEmail();
        String userPhone = sessionManager.getUserPhone();
        String role      = sessionManager.getUserRole();

        // Bind data
        tvName.setText(userName);
        tvEmail.setText(userEmail);
        tvPhone.setText(userPhone);
        tvRole.setText(role);

        // Logout → clear session and navigate to LoginActivity
        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            // optional: pop back to login if using fragment stack
        });
    }
}