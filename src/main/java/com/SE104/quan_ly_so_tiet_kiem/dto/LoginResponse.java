package com.SE104.quan_ly_so_tiet_kiem.dto;
import lombok.Data;

@Data
public class LoginResponse {
    private UserResponse user;
    private String token;
    private String message;

    public LoginResponse() {
    }

    public LoginResponse(UserResponse user, String token) {
        this.user = user;
        this.token = token;
    }

    public LoginResponse(UserResponse user, String token, String message) {
        this.user = user;
        this.token = token;
        this.message = message;
    }
}
