package com.example.floodrescue.api;

public class LoginResponse {
    private String status;
    private String message;
    private UserData user;

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public UserData getUser() { return user; }
}
