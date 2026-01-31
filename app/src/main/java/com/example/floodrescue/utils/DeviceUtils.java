package com.example.floodrescue.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class DeviceUtils {

    // Kotlin object -> static methods in Java

    public static String getUserAgent(Context context) {

        String appName = "FloodRescue/1.0";
        String deviceModel = Build.MODEL;
        String androidVersion = Build.VERSION.RELEASE;
        String deviceId = getDeviceId(context);

        return appName + " (Android " + androidVersion +
                "; " + deviceModel +
                "; DeviceID: " + deviceId + ")";
    }

    private static String getDeviceId(Context context) {
        try {
            String id = Settings.Secure.getString(
                    context.getContentResolver(),
                    Settings.Secure.ANDROID_ID
            );

            return id != null ? id : "unknown_device";

        } catch (Exception e) {
            return "unknown_device";
        }
    }

    public static Map<String, String> getDeviceInfo() {

        Map<String, String> info = new HashMap<>();

        info.put("manufacturer", Build.MANUFACTURER);
        info.put("model", Build.MODEL);
        info.put("product", Build.PRODUCT);
        info.put("device", Build.DEVICE);
        info.put("android_version", Build.VERSION.RELEASE);
        info.put("sdk_version", String.valueOf(Build.VERSION.SDK_INT));
        info.put("locale", Locale.getDefault().toString());
        info.put("timezone", TimeZone.getDefault().getID());

        return info;
    }

    public static boolean isNetworkAvailable(Context context) {
        if (context == null) return false;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                if (capabilities != null) {
                    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                }
            } else {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            }
        }
        return false;
    }
}
