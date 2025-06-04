package com.SE104.quan_ly_so_tiet_kiem.dto;

import java.math.BigDecimal;
import java.time.LocalDate; 
// import java.util.Date; 
import lombok.Data;

@Data
public class GiaoDichDTO { 
    private String maGiaoDichString; 
    private Long idGiaoDich; 
    private String loaiGiaoDich; 
    private BigDecimal soTien;
    private LocalDate ngayGD; 
    // private Date ngayGDUtil;

    private Integer maKhachHang;
    private String tenKhachHang;

    private Integer maSoMoTietKiem; 
    private String tenSoMoTietKiem; 
    private String tenSanPhamSoTietKiem; 

    // private BigDecimal laiSuatTaiThoiDiemGiaoDich; 
}