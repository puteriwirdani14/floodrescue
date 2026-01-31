package com.example.floodrescue.api;

public class SafeLocation {
    private int id;
    private String name;
    private String type;
    private double latitude;
    private double longitude;
    private String description;
    private Integer capacity;

    public int getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getDescription() { return description; }
    public Integer getCapacity() { return capacity; }
}
