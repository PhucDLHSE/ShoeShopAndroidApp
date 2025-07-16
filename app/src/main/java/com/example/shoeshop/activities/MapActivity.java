package com.example.shoeshop.activities;

import android.Manifest;
import android.content.Intent; // Import này cần thiết để quay lại MainActivity
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.widget.ImageButton; // Import ImageButton

import com.example.shoeshop.R;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;

public class MapActivity extends AppCompatActivity {
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView mapView;
    private ImageButton backButton; // Khai báo ImageButton

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(getApplicationContext(), getSharedPreferences("osmdroid", MODE_PRIVATE));
        setContentView(R.layout.activity_map);

        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        // Ánh xạ nút back và thiết lập sự kiện click
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            // Khi nút back được nhấn, kết thúc MapActivity và trở về Activity trước đó (MainActivity)
            finish();
        });

        requestPermissionsIfNecessary();

        mapView.getController().setZoom(16.0);
        GeoPoint startPoint = new GeoPoint(10.762622, 106.660172); // Hồ Chí Minh
        mapView.getController().setCenter(startPoint);

        Marker marker = new Marker(mapView);
        marker.setPosition(startPoint);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle("Vị trí cửa hàng");
        mapView.getOverlays().add(marker);
    }

    private void requestPermissionsIfNecessary() {
        String[] permissions = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE &&
                grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            MyLocationNewOverlay locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
            locationOverlay.enableMyLocation();
            mapView.getOverlays().add(locationOverlay);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume(); //needed for osmdroid
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();  //needed for osmdroid
    }
}