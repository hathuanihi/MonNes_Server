package com.SE104.quan_ly_so_tiet_kiem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyReportDTO {
    private Integer stt;
    private String ngay;          
    private Integer soSoMo;       
    private Integer soSoDong;     
    private Integer chenhLech;    
}
