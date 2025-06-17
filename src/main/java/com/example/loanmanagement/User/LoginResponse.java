package com.example.loanmanagement.User;

public class LoginResponse {

    private String token;
    private int statusCode;
    private String message;
    private UserEntity user;
    private String role;

    // Constructor for successful login (with token + user)
    public LoginResponse(String token, UserEntity user) {
        this.token = token;
        this.statusCode = 200;
        this.message = "Login successful";
        this.role = user.getRole();

        // Sanitize sensitive data
        user.setPassword(null); // Don't expose password
        this.user = user;
    }

    // Constructor for error response
    public LoginResponse(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    // Optional static helpers for quick responses
    public static LoginResponse success(String token, UserEntity user) {
        return new LoginResponse(token, user);
    }

    public static LoginResponse error(int statusCode, String message) {
        return new LoginResponse(statusCode, message);
    }
}
