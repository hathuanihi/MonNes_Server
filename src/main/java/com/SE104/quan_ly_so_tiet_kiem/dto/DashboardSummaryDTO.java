package com.SE104.quan_ly_so_tiet_kiem.dto;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class DashboardSummaryDTO { 
    private BigDecimal tongSoDuTatCaSoCuaUser; 
    private BigDecimal tongTienDaNapThangNay; 
    private BigDecimal tongTienDaRutThangNay; 
    private Integer soLuongSoTietKiemDangHoatDong; 
}