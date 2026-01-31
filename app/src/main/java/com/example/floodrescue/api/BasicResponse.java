package com.example.floodrescue.api;

import com.google.gson.annotations.SerializedName;



public class BasicResponse {
    @SerializedName("status")
    private String status; // Tukar kepada String untuk lebih stabil

    @SerializedName("message")
    private String message;

    public boolean isSuccess() {
        // Terima "success", "true", atau apa-apa yang positif
        return "success".equalsIgnoreCase(status) || "true".equalsIgnoreCase(status);
    }

    public String getMessage() {
        return message != null ? message : "No message from server";
    }
}
