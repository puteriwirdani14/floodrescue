package com.example.floodrescue.api;

public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String phone;

    public RegisterRequest(String name, String email, String password) {
        this(name, email, password, "");
    }

    public RegisterRequest(String name, String email, String password, String phone) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
    }
}
