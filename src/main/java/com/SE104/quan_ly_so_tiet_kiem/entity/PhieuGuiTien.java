package com.SE104.quan_ly_so_tiet_kiem.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "PHIEUGUITIEN")
public class PhieuGuiTien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_phieu") 
    private Integer maPGT; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_mostk", nullable = false) 
    @ToString.Exclude
    private MoSoTietKiem moSoTietKiem;

    @Column(name = "so_tien_gui", nullable = false, precision = 15, scale = 2) 
    private BigDecimal soTienGui;

    @Column(name = "ngay_gui", nullable = false) 
    private LocalDate ngayGui; 
}