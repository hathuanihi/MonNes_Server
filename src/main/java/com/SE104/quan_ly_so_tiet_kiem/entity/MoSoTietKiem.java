package com.SE104.quan_ly_so_tiet_kiem.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Entity
@Table(name = "mosotietkiem")
public class MoSoTietKiem {
    private static final Logger logger = LoggerFactory.getLogger(MoSoTietKiem.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mamstk")
    private Integer maMoSo;

    @Column(name = "ten_so_mo", nullable = true)
    private String tenSoMo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_stk_san_pham", nullable = false)
    private SoTietKiem soTietKiemSanPham;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_nd", nullable = false)
    @ToString.Exclude
    private NguoiDung nguoiDung;

    @Column(name = "ngay_mo", nullable = false)
    private LocalDate ngayMo;

    @Column(name = "ngay_dao_han", nullable = true)
    private LocalDate ngayDaoHan;

    @Column(name = "so_du", nullable = false, precision = 19, scale = 4, columnDefinition = "DECIMAL(19,4) DEFAULT 0.0000")
    private BigDecimal soDu = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", columnDefinition = "enum('DANG_HOAT_DONG','DA_DONG', 'DA_DAO_HAN')", nullable = false)
    private TrangThaiMoSo trangThai = TrangThaiMoSo.DANG_HOAT_DONG;

    @Column(name = "lai_suat_ap_dung", nullable = false, precision = 5, scale = 2)
    private BigDecimal laiSuatApDung;

    @OneToMany(mappedBy = "moSoTietKiem", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<PhieuGuiTien> phieuGuiTienList;

    @OneToMany(mappedBy = "moSoTietKiem", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<PhieuRutTien> phieuRutTienList;

    @OneToMany(mappedBy = "moSoTietKiem", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<GiaoDich> giaoDichList;

    public enum TrangThaiMoSo {
        DANG_HOAT_DONG, DA_DONG, DA_DAO_HAN
    }

    @Column(name = "ngay_tra_lai_cuoi_cung")
    @Temporal(TemporalType.DATE)
    private LocalDate ngayTraLaiCuoiCung;

    @Column(name = "ngay_tra_lai_ke_tiep")
    @Temporal(TemporalType.DATE)
    private LocalDate ngayTraLaiKeTiep;

    @PrePersist
    @PreUpdate
    public void tinhNgayDaoHanVaLaiSuatApDung() {
        if (this.soTietKiemSanPham != null && this.ngayMo != null) {
            this.laiSuatApDung = this.soTietKiemSanPham.getLaiSuat();
            if (this.soTietKiemSanPham.getKyHan() != null && this.soTietKiemSanPham.getKyHan() > 0) {
                this.ngayDaoHan = this.ngayMo.plusMonths(this.soTietKiemSanPham.getKyHan());
                this.ngayTraLaiKeTiep = this.ngayMo.plusMonths(1);
                logger.debug("Setting ngayDaoHan for MoSoTietKiem ID {}: ngayMo={}, kyHan={}, ngayDaoHan={}, ngayTraLaiKeTiep={}", 
                             this.maMoSo, this.ngayMo, this.soTietKiemSanPham.getKyHan(), this.ngayDaoHan, this.ngayTraLaiKeTiep);
            } else {
                this.ngayDaoHan = null;
                // Đối với sổ không kỳ hạn, tính ngayTraLaiKeTiep dựa trên số ngày gửi tối thiểu
                int soNgayGuiToiThieu = this.soTietKiemSanPham.getSoNgayGuiToiThieuDeRut() != null ? 
                    this.soTietKiemSanPham.getSoNgayGuiToiThieuDeRut() : 15;
                // ngayTraLaiKeTiep = ngayMo + soNgayGuiToiThieu + 1 ngày để có thể tính lãi
                this.ngayTraLaiKeTiep = this.ngayMo.plusDays(soNgayGuiToiThieu + 1);
                logger.debug("Setting non-term account for MoSoTietKiem ID {}: ngayMo={}, ngayDaoHan=null, soNgayGuiToiThieu={}, ngayTraLaiKeTiep={}", 
                             this.maMoSo, this.ngayMo, soNgayGuiToiThieu, this.ngayTraLaiKeTiep);
            }
        } else {
            logger.warn("Cannot set ngayDaoHan for MoSoTietKiem ID {}: soTietKiemSanPham={}, ngayMo={}", 
                        this.maMoSo, this.soTietKiemSanPham, this.ngayMo);
        }
    }

    public MoSoTietKiem() {}

    public MoSoTietKiem(String tenSoMo, SoTietKiem soTietKiemSanPham, NguoiDung nguoiDung, LocalDate ngayMo, BigDecimal soTienGuiBanDau) {
        this.tenSoMo = tenSoMo;
        this.soTietKiemSanPham = soTietKiemSanPham;
        this.nguoiDung = nguoiDung;
        this.ngayMo = ngayMo;
        this.trangThai = TrangThaiMoSo.DANG_HOAT_DONG;
        if (soTietKiemSanPham != null) {
            this.laiSuatApDung = soTietKiemSanPham.getLaiSuat();
        }
        tinhNgayDaoHanVaLaiSuatApDung();
    }
}