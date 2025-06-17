package com.SE104.quan_ly_so_tiet_kiem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyReportDTO {
    private Integer stt;
    private String loaiTietKiem;  
    private BigDecimal tongThu;  
    private BigDecimal tongChi;   
    private BigDecimal chenhLech; 
}
