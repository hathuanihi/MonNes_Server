package com.SE104.quan_ly_so_tiet_kiem.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "THAYDOI_QUYDINH_SOTIETKIEM") 
public class ThayDoi { 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "matd")
    private Integer maTD;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_nd_admin", nullable = false) 
    @ToString.Exclude
    private NguoiDung nguoiDungAdmin; 

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "ma_stk_san_pham", nullable = false)
    @ToString.Exclude
    private SoTietKiem soTietKiemSanPham; 

    @Column(name = "ten_so_cu", length = 255)
    private String tenSoCu;
    @Column(name = "ky_han_cu")
    private Integer kyHanCu;
    @Column(name = "lai_suat_cu", precision = 5, scale = 2)
    private BigDecimal laiSuatCu;
    @Column(name = "tien_gui_ban_dau_toi_thieu_cu")
    private Long tienGuiBanDauToiThieuCu;
    @Column(name = "tien_gui_them_toi_thieu_cu")
    private Long tienGuiThemToiThieuCu;
    @Column(name = "so_ngay_gui_toi_thieu_de_rut_cu")
    private Integer soNgayGuiToiThieuDeRutCu;

    @Column(name = "ten_so_moi", length = 255)
    private String tenSoMoi;
    @Column(name = "ky_han_moi")
    private Integer kyHanMoi;
    @Column(name = "lai_suat_moi", precision = 5, scale = 2)
    private BigDecimal laiSuatMoi;
    @Column(name = "tien_gui_ban_dau_toi_thieu_moi")
    private Long tienGuiBanDauToiThieuMoi;
    @Column(name = "tien_gui_them_toi_thieu_moi")
    private Long tienGuiThemToiThieuMoi;
    @Column(name = "so_ngay_gui_toi_thieu_de_rut_moi")
    private Integer soNgayGuiToiThieuDeRutMoi;


    @Column(name = "ngay_thay_doi", nullable = false)
    private LocalDate ngayThayDoi;

    @Column(name = "ghi_chu", length = 500)
    private String ghiChu; 
}