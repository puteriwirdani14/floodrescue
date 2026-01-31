package com.example.floodrescue.models;

import com.google.gson.annotations.SerializedName;

public class SafeLocation {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("incident_type")
    private String type;

    @SerializedName("severity")
    private String severity;

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getType() { return type; }
    public String getSeverity() { return severity; }
}
