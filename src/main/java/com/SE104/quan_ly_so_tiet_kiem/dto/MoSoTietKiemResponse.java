package com.SE104.quan_ly_so_tiet_kiem.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class MoSoTietKiemResponse { 
    private Integer maMoSo;
    private String tenSoMo; 
    private LocalDate ngayMo;
    private LocalDate ngayDaoHan;
    private BigDecimal soDuHienTai; 
    private String trangThaiMoSo; // DANG_HOAT_DONG, DA_DONG

    private String tenNguoiDung;

    private Integer maSanPhamSoTietKiem;
    private String tenSanPhamSoTietKiem;
    private Integer kyHanSanPham; // tháng
    private BigDecimal laiSuatSanPhamHienTai; 
    private BigDecimal laiSuatApDungChoSoNay; 

}