package com.SE104.quan_ly_so_tiet_kiem.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ThongKeDTO { 
    private Long luotTruyCapHomNay;
    private Long luotTruyCapThangNay; 
    private BigDecimal doanhThuHomNay; 
    private BigDecimal doanhThuThangNay; 

    private Long tongSoNguoiDung;
    private Long tongSoTaiKhoanTietKiemDangHoatDong;
    private BigDecimal tongSoDuToanHeThong; 

    private List<GiaoDichDTO> giaoDichGanDayNhat; 
}