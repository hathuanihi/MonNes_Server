package com.SE104.quan_ly_so_tiet_kiem.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SoTietKiemDTO { 

    private Integer maSo; 
    private String tenSo; 
    private Integer kyHan; 
    private BigDecimal laiSuat; 
    private Long tienGuiBanDauToiThieu;
    private Long tienGuiThemToiThieu;
    private Integer soNgayGuiToiThieuDeRut; 

    private LoaiSoTietKiemDTO loaiSoTietKiemDanhMuc; 
    // private String tenLoaiSoTietKiemDanhMuc; 
}