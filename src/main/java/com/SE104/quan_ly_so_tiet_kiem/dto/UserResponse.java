package com.SE104.quan_ly_so_tiet_kiem.dto;

import lombok.Data;
import java.sql.Date; 

@Data
public class UserResponse {
    private Integer id; 
    private String tenND;
    private String cccd;
    private String diaChi;
    private String sdt;
    private String email;
    private Date ngaySinh; 
    private String vaiTro; // ADMIN, USER
}