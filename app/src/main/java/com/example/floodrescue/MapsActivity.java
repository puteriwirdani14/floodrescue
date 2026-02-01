package com.example.floodrescue;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.floodrescue.api.ApiClient;
import com.example.floodrescue.models.Report;
import com.example.floodrescue.models.SafeLocation;
import com.example.floodrescue.ui.ReportDialog;
import com.example.floodrescue.utils.SharedPrefManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST = 100;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;

    private String userName = "User";
    private TextView tvHello;
    private TextView tvLocation;
    private double lastLat, lastLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SharedPrefManager pref = new SharedPrefManager(this);
        String tempName = getIntent().getStringExtra("user_name");
        if (tempName == null || tempName.trim().isEmpty()) {
            tempName = pref.getUserName();
        }
        if (tempName != null && !tempName.trim().isEmpty()) {
            userName = tempName.trim();
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("FloodRescue Map");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tvHello = findViewById(R.id.textHello);
        tvLocation = findViewById(R.id.textLocation);

        if (tvHello != null) tvHello.setText("Hello " + userName + "!");
        if (tvLocation != null) tvLocation.setText("Detecting your location...");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // New Buttons logic
        Button btnReport = findViewById(R.id.btnReportIncident);
        Button btnList = findViewById(R.id.btnIncidentList);

        btnReport.setOnClickListener(v -> {
            if (lastLat == 0 && lastLng == 0) {
                Toast.makeText(this, "Wait for location detection...", Toast.LENGTH_SHORT).show();
            } else {
                ReportDialog dialog = new ReportDialog(lastLat, lastLng, () -> {
                    if (mMap != null) {
                        mMap.clear();
                        loadUserReports();
                        loadShelters();
                    }
                });
                dialog.show(getSupportFragmentManager(), "ReportDialog");
            }
        });

        btnList.setOnClickListener(v -> {
            Intent intent = new Intent(this, IncidentListActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }

        enableUserLocation();
        loadUserReports();
        loadShelters();
    }

    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.setMyLocationEnabled(true);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                lastLat = location.getLatitude();
                lastLng = location.getLongitude();
                LatLng currentLatLng = new LatLng(lastLat, lastLng);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));
                updateAddressText(lastLat, lastLng);
            } else {
                if (tvLocation != null) tvLocation.setText("Location not found. Please enable GPS.");
            }
        });
    }

    private void updateAddressText(double lat, double lng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String addressString = address.getAddressLine(0);
                if (tvLocation != null) {
                    tvLocation.setText(addressString);
                }
            } else {
                if (tvLocation != null) tvLocation.setText("Lat: " + lat + ", Lng: " + lng);
            }
        } catch (IOException e) {
            Log.e("MapsActivity", "Geocoder failed: " + e.getMessage());
            if (tvLocation != null) tvLocation.setText("Lat: " + lat + ", Lng: " + lng);
        }
    }

    private void loadUserReports() {
        ApiClient.getInstance().getLocations().enqueue(new Callback<List<Report>>() {
            @Override
            public void onResponse(Call<List<Report>> call, Response<List<Report>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Report report : response.body()) {
                        LatLng pos = new LatLng(report.getLatitude(), report.getLongitude());
                        mMap.addMarker(new MarkerOptions()
                                .position(pos)
                                .title("Report: " + report.getUserName())
                                .snippet(report.getIncidentType() + " - " + report.getDescription())
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Report>> call, Throwable t) {
                Log.e("MapsActivity", "Reports failed: " + t.getMessage());
            }
        });
    }

    private void loadShelters() {
        ApiClient.getInstance().getShelters().enqueue(new Callback<List<SafeLocation>>() {
            @Override
            public void onResponse(Call<List<SafeLocation>> call, Response<List<SafeLocation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (SafeLocation loc : response.body()) {
                        LatLng pos = new LatLng(loc.getLatitude(), loc.getLongitude());
                        mMap.addMarker(new MarkerOptions()
                                .position(pos)
                                .title("SHELTER: " + loc.getName())
                                .snippet(loc.getDescription())
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                    }
                }
            }

            @Override
            public void onFailure(Call<List<SafeLocation>> call, Throwable t) {
                Log.e("MapsActivity", "Shelters failed: " + t.getMessage());
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) enableUserLocation();
        }
    }
}
