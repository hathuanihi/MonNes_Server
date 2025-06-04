package com.SE104.quan_ly_so_tiet_kiem.dto;

import java.math.BigDecimal; 
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class UserDetailDTO { 

    private Integer maND;
    private String tenND;
    private String cccd;
    private String diaChi;
    private String sdt;
    private Date ngaySinh; 
    private String email;
    private String vaiTro; // ADMIN, USER

    private List<MoSoTietKiemResponse> danhSachSoTietKiemDaMo;

    private BigDecimal tongSoDuTatCaSo;
}