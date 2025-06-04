package com.SE104.quan_ly_so_tiet_kiem.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.SE104.quan_ly_so_tiet_kiem.model.TransactionType;

@Data
@Entity
@Table(name = "GIAO_DICH") 
public class GiaoDich {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "so_tien", nullable = false, precision = 19, scale = 4)
    private BigDecimal soTien;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_giao_dich", nullable = false, columnDefinition = "enum('DEPOSIT','WITHDRAW')")
    private TransactionType loaiGiaoDich;

    @Column(name = "ngay_thuc_hien", nullable = false)
    private LocalDate ngayThucHien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mo_so_tiet_kiem_id", nullable = false)
    @ToString.Exclude
    private MoSoTietKiem moSoTietKiem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "san_pham_so_tiet_kiem_id", nullable = false) 
    @ToString.Exclude
    private SoTietKiem sanPhamSoTietKiem; 

    public GiaoDich() {}

    public GiaoDich(BigDecimal soTien, TransactionType loaiGiaoDich, MoSoTietKiem moSoTietKiem) {
        this.soTien = soTien;
        this.loaiGiaoDich = loaiGiaoDich;
        this.ngayThucHien = LocalDate.now();
        this.moSoTietKiem = moSoTietKiem;
        if (moSoTietKiem != null && moSoTietKiem.getSoTietKiemSanPham() != null) {
            this.sanPhamSoTietKiem = moSoTietKiem.getSoTietKiemSanPham(); 
        } else {
            throw new IllegalStateException("Không thể xác định sản phẩm sổ tiết kiệm cho giao dịch khi MoSoTietKiem hoặc sản phẩm của nó là null.");
        }
    }
}