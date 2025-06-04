package com.SE104.quan_ly_so_tiet_kiem.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.TemporalType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@Entity
@Table(name = "DANGNHAP")
public class DangNhap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "madn")
    private Integer maDN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mand", nullable = false)
    @ToString.Exclude
    private NguoiDung nguoiDung;

    // private String matKhau;

    @Column(name = "login_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date loginTime;
}