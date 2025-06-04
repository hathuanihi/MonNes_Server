package com.SE104.quan_ly_so_tiet_kiem.dto;
import lombok.Data;

@Data
public class LoginRequest {
    private String username; // có thể là email hoặc số điện thoại
    private String password;
}
