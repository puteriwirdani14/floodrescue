package com.example.floodrescue.models;

import com.google.gson.annotations.SerializedName;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Report {

    @SerializedName("id")
    private int id;

    @SerializedName("user_name") // Padankan dengan column di database
    private String userName;

    @SerializedName("incident_type")
    private String incident_type;

    @SerializedName("description")
    private String description;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("severity")
    private String severity;

    @SerializedName("status")
    private String status;

    @SerializedName("created_at")
    private String createdAt;

    public Report() {} // Constructor kosong diperlukan oleh GSON

    // ===== Getters =====
    public int getId() { return id; }
    public String getUserName() { return userName; }
    public String getIncidentType() { return incident_type; }
    public String getDescription() { return description; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getSeverity() { return severity; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
}
