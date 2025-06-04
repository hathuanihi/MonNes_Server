package com.SE104.quan_ly_so_tiet_kiem.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "loaisotietkiem_danhmuc") 
public class LoaiSoTietKiem { 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_loai_danh_muc")
    private Integer maLoaiDanhMuc;

    @Column(name = "ten_loai_danh_muc", unique = true, nullable = false)
    private String tenLoaiDanhMuc; 

    // @Column(name = "ky_han", nullable = false)
    // private Integer kyHan;

    // @Column(name = "lai_suat", nullable = false, precision = 5, scale = 2)
    // private BigDecimal laiSuat;

    // @Column(name = "tien_gui_toi_thieu", nullable = false)
    // private Long tienGuiToiThieu;

    // @Column(name = "tien_gui_them_toi_thieu", nullable = false)
    // private Long tienGuiThemToiThieu;

    // @Column(name = "so_ngay_gui_toi_thieu", nullable = false)
    // private Integer soNgayGuiToiThieu;

    @OneToMany(mappedBy = "loaiSoTietKiemDanhMuc", cascade = CascadeType.ALL)
    private List<SoTietKiem> danhSachSanPhamSoTietKiem;

    public LoaiSoTietKiem() {
    }

    // public LoaiSoTietKiem(String tenLoaiDanhMuc) { 
    //    this.tenLoaiDanhMuc = tenLoaiDanhMuc;
    // }
}