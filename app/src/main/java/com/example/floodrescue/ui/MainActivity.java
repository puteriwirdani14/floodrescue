package com.example.floodrescue.ui;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.floodrescue.R;
import com.example.floodrescue.databinding.ActivityMainBinding;
import com.example.floodrescue.utils.DeviceUtils;
import com.example.floodrescue.utils.SharedPrefManager;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SharedPrefManager sharedPref;
    private static final String CHANNEL_ID = "connectivity_channel";
    private static final int NOTIFICATION_ID = 101;

    // Launcher for requesting notification permission on Android 13+
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    checkInternetAndNotify();
                } else {
                    Toast.makeText(this, "Notification permission is required for internet alerts.", Toast.LENGTH_SHORT).show();
                }
            });

    private final BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkInternetAndNotify();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPref = new SharedPrefManager(this);

        createNotificationChannel();
        requestNotificationPermission();

        // Edge-to-edge handling
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );
            return insets;
        });

        // Get username
        String userName = getIntent().getStringExtra("user_name");
        if (userName == null || userName.trim().isEmpty()) {
            userName = sharedPref.getUserName();
        }

        binding.textWelcome.setText("Welcome, " + userName + "!");
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        checkInternetAndNotify(); // Initial check when app comes to foreground
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkReceiver);
    }

    private void checkInternetAndNotify() {
        if (!DeviceUtils.isNetworkAvailable(this)) {
            showNoInternetNotification();
        } else {
            cancelNotification();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Connectivity Status";
            String description = "Notifications about network connectivity";
            int importance = NotificationManager.IMPORTANCE_HIGH; // Higher importance for visibility
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void showNoInternetNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_notify_error) // Clearer error icon
                .setContentTitle("Connection Lost")
                .setContentText("FloodRescue requires an active internet connection.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true) // User can't swipe it away until internet returns
                .setCategory(NotificationCompat.CATEGORY_SERVICE);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    private void cancelNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_ID);
        }
    }


}
