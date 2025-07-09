package com.example.shoeshop.models;

public class LoginResponse {
    private String message;
    private User user;
    private String token;
    private String refreshToken;

    public String getMessage() { return message; }
    public User getUser() { return user; }
    public String getToken() { return token; }
    public String getRefreshToken() { return refreshToken; }
}
