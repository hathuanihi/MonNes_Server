package com.SE104.quan_ly_so_tiet_kiem.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date; 

@Data
@Entity
@Table(name = "PHIEURUTTIEN")
public class PhieuRutTien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maprt")
    private Integer maPRT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mamstk", nullable = false) 
    @ToString.Exclude
    private MoSoTietKiem moSoTietKiem;

    @Column(name = "so_tien_rut", nullable = false, precision = 19, scale = 4) 
    private BigDecimal soTienRut;

    @Temporal(TemporalType.TIMESTAMP) 
    @Column(name = "ngay_rut", nullable = false) 
    private Date ngayRut;

    @Column(name = "lai_suat_khi_rut", precision = 5, scale = 2, nullable = true)
    private BigDecimal laiSuatKhiRut;

    @Column(name = "tien_lai_thuc_nhan", precision = 19, scale = 4, nullable = true)
    private BigDecimal tienLaiThucNhan; 
}