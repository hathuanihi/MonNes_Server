package com.SE104.quan_ly_so_tiet_kiem.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.Date; 
import java.util.List;

@Data
@Entity
@Table(name = "NGUOIDUNG")
public class NguoiDung {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mand")
    private Integer maND;

    @Column(name = "tennd") 
    private String tenND;

    // @NotBlank(message = "CCCD không được để trống") 
    // @Pattern(regexp = "\\d{12}", message = "CCCD phải có 12 chữ số") 
    @Column(name = "cccd", unique = true) 
    private String cccd;

    // @NotBlank(message = "Địa chỉ không được để trống") 
    @Column(name = "dia_chi") 
    private String diaChi;

    // @NotBlank(message = "Số điện thoại không được để trống") 
    // @Pattern(regexp = "0\\d{9}", message = "Số điện thoại không hợp lệ") 
    @Column(name = "sdt", unique = true) 
    private String sdt;

    // @Temporal(TemporalType.TIMESTAMP) 
    @Temporal(TemporalType.DATE)
    // @NotBlank(message = "Ngày sinh không được để trống") 
    @Column(name = "ngay_sinh") 
    private Date ngaySinh;

    // @NotBlank(message = "Email không được để trống") 
    // @Email(message = "Email không hợp lệ") 
    @Column(name = "email", unique = true, nullable = false) 
    private String email;

    // @NotBlank(message = "Mật khẩu không được để trống") 
    @Column(name = "mat_khau", nullable = false)
    private String matKhau;

    @Column(name = "vai_tro", nullable = false)
    private Integer vaiTro; // 0: admin, 1: user

    @OneToMany(mappedBy = "nguoiDung", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<DangNhap> dangNhapList;

    @OneToMany(mappedBy = "nguoiDungAdmin", cascade = CascadeType.ALL, orphanRemoval = true) 
    @ToString.Exclude
    private List<ThayDoi> thayDoiList;

    @OneToMany(mappedBy = "nguoiDung", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<MoSoTietKiem> moSoTietKiemList;
}