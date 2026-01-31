package com.example.floodrescue.utils;

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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public abstract class BaseActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "connectivity_channel";
    private static final int NOTIFICATION_ID = 101;
    private static final int RESTORED_NOTIFICATION_ID = 102;
    private static boolean isCurrentlyOffline = false;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    checkInternetAndNotify(false);
                }
            });

    private final BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkInternetAndNotify(true);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createNotificationChannel();
        requestNotificationPermission();
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
        checkInternetAndNotify(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkReceiver);
    }

    private void checkInternetAndNotify(boolean isFromReceiver) {
        boolean isOnline = DeviceUtils.isNetworkAvailable(this);
        
        if (!isOnline) {
            showNoInternetNotification();
            isCurrentlyOffline = true;
        } else {
            cancelNotification(NOTIFICATION_ID);
            // Only show "Back Online" if we were previously offline and this is a live change
            if (isCurrentlyOffline && isFromReceiver) {
                showInternetRestoredNotification();
                isCurrentlyOffline = false;
            } else if (isOnline) {
                isCurrentlyOffline = false;
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Connectivity Status";
            String description = "Notifications about network connectivity";
            int importance = NotificationManager.IMPORTANCE_HIGH;
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
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setContentTitle("Connection Lost")
                .setContentText("FloodRescue requires an active internet connection.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .setColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    private void showInternetRestoredNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle("Connection Restored")
                .setContentText("You are back online. All features are available.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true) // Disappears when tapped
                .setTimeoutAfter(3000) // Automatically disappears after 3 seconds
                .setColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(RESTORED_NOTIFICATION_ID, builder.build());
        }
    }

    private void cancelNotification(int id) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(id);
        }
    }
}
