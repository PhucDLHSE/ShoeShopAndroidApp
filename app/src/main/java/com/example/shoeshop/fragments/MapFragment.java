package com.example.shoeshop.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.shoeshop.R;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;

public class MapFragment extends Fragment {

    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView mapView;
    private ImageView btnButton;

    public MapFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Configuration.getInstance().load(requireContext(), requireActivity().getSharedPreferences("osmdroid", getContext().MODE_PRIVATE));
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mapView = view.findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        requestPermissionsIfNecessary();

        mapView.getController().setZoom(16.0);
        GeoPoint startPoint = new GeoPoint(10.818795, 106.651800);
        mapView.getController().setCenter(startPoint);

        Marker marker = new Marker(mapView);
        marker.setPosition(startPoint);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle("Đ. Trường Sơn, P. Tân, Sơn Hoà, Hồ Chí Minh, Việt Nam");
        mapView.getOverlays().add(marker);

        return view;
    }

    private void requestPermissionsIfNecessary() {
        String[] permissions = new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        ActivityCompat.requestPermissions(requireActivity(), permissions, REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE &&
                grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            MyLocationNewOverlay locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(requireContext()), mapView);
            locationOverlay.enableMyLocation();
            mapView.getOverlays().add(locationOverlay);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }
}
