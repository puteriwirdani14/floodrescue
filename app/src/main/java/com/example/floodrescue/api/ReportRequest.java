package com.example.floodrescue.api;

public class ReportRequest {
    private String user_name;
    private String incident_type;
    private double latitude;
    private double longitude;
    private String description;
    private String severity;

    public ReportRequest(String user_name,
                         String incident_type,
                         double latitude,
                         double longitude,
                         String description,
                         String severity) {
        this.user_name = user_name;
        this.incident_type = incident_type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.severity = severity;
    }
}
