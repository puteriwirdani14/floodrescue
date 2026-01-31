package com.example.floodrescue;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.floodrescue.auth.LoginActivity;
import com.example.floodrescue.ui.ReportDialog;
import com.example.floodrescue.utils.BaseActivity;
import com.example.floodrescue.utils.SharedPrefManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;

import java.util.List;
import java.util.Locale;

public class WelcomeActivity extends BaseActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;

    private SharedPrefManager pref;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setContentView(R.layout.activity_welcome);

        pref = new SharedPrefManager(this);

        String tempName = getIntent().getStringExtra("user_name");
        if (tempName == null || tempName.isEmpty()) {
            tempName = pref.getUserName();
        }
        userName = tempName;

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        Button btnContinue = findViewById(R.id.btnContinue);
        Button btnReport = findViewById(R.id.btnReport);
        
        // Emergency Buttons
        View btnCall999 = findViewById(R.id.btnCall999);
        View btnCallBomba = findViewById(R.id.btnCallBomba);
        View btnCallCivil = findViewById(R.id.btnCallCivil);

        tvWelcome.setText("Welcome, " + userName + "!");
        btnReport.setText("Open Report");

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // ---------------- UPDATE DRAWER INFO ----------------
        updateDrawerHeaderInfo();

        btnContinue.setOnClickListener(v -> openMap());
        btnReport.setOnClickListener(v -> checkPermissionAndOpenReport());
        
        // Handle Emergency Calls
        if (btnCall999 != null) btnCall999.setOnClickListener(v -> makeCall("999"));
        if (btnCallBomba != null) btnCallBomba.setOnClickListener(v -> makeCall("994")); // Bomba specific line
        if (btnCallCivil != null) btnCallCivil.setOnClickListener(v -> makeCall("999")); // Or specific APM number

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                drawerLayout.closeDrawers();
            } else if (id == R.id.nav_map) {
                drawerLayout.closeDrawers();
                openMap();
            } else if (id == R.id.nav_report) {
                drawerLayout.closeDrawers();
                checkPermissionAndOpenReport();
            } else if (id == R.id.nav_about) {
                drawerLayout.closeDrawers();
                startActivity(new Intent(this, AboutUsActivity.class));
            } else if (id == R.id.nav_logout) {
                drawerLayout.closeDrawers();
                doLogout();
            }
            return true;
        });
    }

    private void makeCall(String number) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + number));
        startActivity(intent);
    }

    // Inflate the toolbar menu (the Info/About icon in top right)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.welcome_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_about) {
            startActivity(new Intent(this, AboutUsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateDrawerHeaderInfo() {
        View headerView = navigationView.getHeaderView(0);
        TextView tvEmail = headerView.findViewById(R.id.tvDrawerEmail);
        
        String userEmail = pref.getUserEmail();
        if (userEmail != null && !userEmail.isEmpty()) {
            tvEmail.setText(userEmail);
        } else {
            tvEmail.setText("No email available");
        }
    }

    private void checkPermissionAndOpenReport() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
        } else {
            fetchLocationAndOpenDialog();
        }
    }

    private void fetchLocationAndOpenDialog() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    showLocationConfirmation(location.getLatitude(), location.getLongitude());
                } else {
                    Toast.makeText(this, "Turn on GPS and try again.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showLocationConfirmation(double lat, double lng) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_location_confirmation, null);
        bottomSheetDialog.setContentView(view);

        TextView tvName = view.findViewById(R.id.tvLocationName);
        TextView tvDetails = view.findViewById(R.id.tvLocationDetails);
        Button btnConfirm = view.findViewById(R.id.btnConfirmLocation);

        btnConfirm.setText("Yes, Request Help Now");

        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                tvName.setText(address.getFeatureName());
                tvDetails.setText(address.getAddressLine(0));
            }
        } catch (Exception e) {
            tvName.setText("Current Location");
            tvDetails.setText(String.format(Locale.getDefault(), "Lat: %.6f, Lng: %.6f", lat, lng));
        }

        btnConfirm.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            openReportDialog(lat, lng);
        });

        bottomSheetDialog.show();
    }

    private void openReportDialog(double lat, double lng) {
        ReportDialog reportDialog = new ReportDialog(lat, lng, () -> {
            Toast.makeText(WelcomeActivity.this, "Emergency Help Request Sent!", Toast.LENGTH_LONG).show();
        });
        reportDialog.show(getSupportFragmentManager(), "ReportDialogTag");
    }

    private void openMap() {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("user_name", userName);
        startActivity(intent);
    }

    private void doLogout() {
        // Sign out from Google so the account chooser shows up next time
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            pref.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
