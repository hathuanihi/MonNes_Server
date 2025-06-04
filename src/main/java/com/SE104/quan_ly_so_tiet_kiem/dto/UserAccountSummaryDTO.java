package com.SE104.quan_ly_so_tiet_kiem.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UserAccountSummaryDTO { 
    private BigDecimal tongSoDuTrongTatCaSo;
    private BigDecimal tongTienDaNapTuTruocDenNay; 
    private BigDecimal tongTienDaRutTuTruocDenNay; 
    private Integer tongSoLuongSoTietKiemDaMo; 
    private Integer tongSoGiaoDichDaThucHien;
}