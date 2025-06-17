package com.SE104.quan_ly_so_tiet_kiem.repository;

import com.SE104.quan_ly_so_tiet_kiem.entity.MoSoTietKiem;
import com.SE104.quan_ly_so_tiet_kiem.entity.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MoSoTietKiemRepository extends JpaRepository<MoSoTietKiem, Integer> {

    List<MoSoTietKiem> findByNguoiDung_MaND(Integer maND);

    Optional<MoSoTietKiem> findByMaMoSoAndNguoiDung_MaND(Integer maMoSo, Integer maND);

    List<MoSoTietKiem> findByTrangThai(MoSoTietKiem.TrangThaiMoSo trangThai);

    List<MoSoTietKiem> findByNguoiDung(NguoiDung nguoiDung);
    
    List<MoSoTietKiem> findByNguoiDung_MaNDAndTrangThai(Integer maND, MoSoTietKiem.TrangThaiMoSo trangThai);
    List<MoSoTietKiem> findByNguoiDungAndTrangThai(NguoiDung nguoiDung, MoSoTietKiem.TrangThaiMoSo trangThai);

    Long countByTrangThai(MoSoTietKiem.TrangThaiMoSo trangThai);

    Integer countByNguoiDung_MaNDAndTrangThai(Integer maND, MoSoTietKiem.TrangThaiMoSo trangThai);

    Integer countByNguoiDungAndTrangThai(NguoiDung nguoiDung, MoSoTietKiem.TrangThaiMoSo trangThai);


    @Query("SELECT SUM(m.soDu) FROM MoSoTietKiem m WHERE m.trangThai = :trangThai")
    BigDecimal sumSoDuByTrangThai(@Param("trangThai") MoSoTietKiem.TrangThaiMoSo trangThai);

    boolean existsBySoTietKiemSanPham_MaSo(Integer maSoSanPham);

    List<MoSoTietKiem> findByNgayDaoHanAndTrangThai(LocalDate ngayDaoHan, MoSoTietKiem.TrangThaiMoSo trangThai);

    @Query("SELECT m FROM MoSoTietKiem m WHERE m.trangThai = :trangThai AND m.ngayTraLaiKeTiep <= :ngayTraLaiKeTiep AND m.soTietKiemSanPham.kyHan = 0")
    List<MoSoTietKiem> findByTrangThaiAndNgayTraLaiKeTiepLessThanEqualAndSoTietKiemSanPham_KyHanIsNull(
        @Param("trangThai") MoSoTietKiem.TrangThaiMoSo trangThai, @Param("ngayTraLaiKeTiep") LocalDate ngayTraLaiKeTiep);
    
    List<MoSoTietKiem> findByTrangThaiAndNgayDaoHanLessThanEqual(MoSoTietKiem.TrangThaiMoSo trangThai, LocalDate date);
    
    List<MoSoTietKiem> findBySoTietKiemSanPham_KyHanAndTrangThai(Integer kyHan, MoSoTietKiem.TrangThaiMoSo trangThai);
    
    @Query("SELECT m FROM MoSoTietKiem m WHERE m.trangThai = :trangThai AND m.ngayTraLaiKeTiep <= :ngayTraLaiKeTiep AND m.soTietKiemSanPham.kyHan > 0")
    List<MoSoTietKiem> findByTrangThaiAndNgayTraLaiKeTiepLessThanEqualAndSoTietKiemSanPham_KyHanIsNotNull(
        @Param("trangThai") MoSoTietKiem.TrangThaiMoSo trangThai, @Param("ngayTraLaiKeTiep") LocalDate ngayTraLaiKeTiep);

    Optional<MoSoTietKiem> findByMaMoSoAndNguoiDung(Integer maMoSo, NguoiDung nguoiDung);

    List<MoSoTietKiem> findByNguoiDungAndTrangThaiAndSoTietKiemSanPham_KyHan(NguoiDung nguoiDung, MoSoTietKiem.TrangThaiMoSo trangThai, Integer kyHan);
    List<MoSoTietKiem> findByTrangThaiAndNgayDaoHan(MoSoTietKiem.TrangThaiMoSo trangThai, LocalDate ngayDaoHan);

    List<MoSoTietKiem> findByTrangThaiAndNgayTraLaiKeTiepLessThanEqual(
        MoSoTietKiem.TrangThaiMoSo trangThai, LocalDate ngayTraLaiKeTiep);

    List<MoSoTietKiem> findBySoTietKiemSanPham_KyHanAndTrangThaiAndNgayTraLaiKeTiepLessThanEqual(
        Integer kyHan, MoSoTietKiem.TrangThaiMoSo trangThai, LocalDate ngayTraLaiKeTiep);

    List<MoSoTietKiem> findBySoTietKiemSanPham_KyHanGreaterThanAndTrangThaiAndNgayTraLaiKeTiepLessThanEqual(
        Integer kyHan, MoSoTietKiem.TrangThaiMoSo trangThai, LocalDate ngayTraLaiKeTiep);

    // New methods for BM5.2 monthly report
    Long countByNgayMo(LocalDate ngayMo);
    
    Long countByTrangThaiAndNgayMo(MoSoTietKiem.TrangThaiMoSo trangThai, LocalDate ngayMo);
}