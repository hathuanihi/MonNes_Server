package com.SE104.quan_ly_so_tiet_kiem.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "sotietkiem") 
public class SoTietKiem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mastk")
    private Integer maSo; 

    @Column(name = "ten_so", nullable = false, unique = true)
    private String tenSo; 

    @Column(name = "ky_han", nullable = false)
    private Integer kyHan;

    @Column(name = "lai_suat", nullable = false, precision = 5, scale = 2)
    private BigDecimal laiSuat;

    @Column(name = "tien_gui_ban_dau_toi_thieu", nullable = false)
    private Long tienGuiBanDauToiThieu;

    @Column(name = "tien_gui_them_toi_thieu", nullable = false)
    private Long tienGuiThemToiThieu;

    @Column(name = "so_ngay_gui_toi_thieu_de_rut") 
    private Integer soNgayGuiToiThieuDeRut;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_loai_danh_muc") 
    private LoaiSoTietKiem loaiSoTietKiemDanhMuc; 
}