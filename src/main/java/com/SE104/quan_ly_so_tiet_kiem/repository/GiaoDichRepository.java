package com.SE104.quan_ly_so_tiet_kiem.repository;

import com.SE104.quan_ly_so_tiet_kiem.entity.GiaoDich;
import com.SE104.quan_ly_so_tiet_kiem.entity.MoSoTietKiem;
import com.SE104.quan_ly_so_tiet_kiem.model.TransactionType;


import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Repository
public interface GiaoDichRepository extends JpaRepository<GiaoDich, Long>, JpaSpecificationExecutor<GiaoDich> {

        boolean existsBySanPhamSoTietKiem_MaSo(Integer maSo);

        // List<GiaoDich> findAll(Specification spec, Pageable pageable);

        List<GiaoDich> findByLoaiGiaoDich(TransactionType loai);

        @Query("SELECT SUM(g.soTien) FROM GiaoDich g " +
                "WHERE g.moSoTietKiem.nguoiDung.maND = :maND " +
                "AND g.loaiGiaoDich = :loaiGD " +
                "AND g.ngayThucHien BETWEEN :startDate AND :endDate")
        BigDecimal sumSoTienByNguoiDungAndLoaiGiaoDichAndDateRange(
                @Param("maND") Integer maND,
                @Param("loaiGD") TransactionType loaiGD,
                @Param("startDate") LocalDate startDate,
                @Param("endDate") LocalDate endDate
        );

        @Query("SELECT g FROM GiaoDich g WHERE g.moSoTietKiem.nguoiDung.maND = :maND ORDER BY g.ngayThucHien DESC, g.id DESC")
        List<GiaoDich> findRecentTransactionsByNguoiDungMaND(@Param("maND") Integer maND, Pageable pageable);

        @Query("SELECT g FROM GiaoDich g WHERE g.id = :giaoDichId AND g.moSoTietKiem.nguoiDung.maND = :maND")
        Optional<GiaoDich> findByIdAndNguoiDungMaND(@Param("giaoDichId") Long giaoDichId, @Param("maND") Integer maND);

        @Query("SELECT g FROM GiaoDich g WHERE g.moSoTietKiem.maMoSo = :maMoSo AND g.moSoTietKiem.nguoiDung.maND = :maND ORDER BY g.ngayThucHien DESC, g.id DESC")
        List<GiaoDich> findByMoSoTietKiem_MaMoSoAndNguoiDung_MaND(
                @Param("maMoSo") Integer maMoSo,
                @Param("maND") Integer maND
        );
        
        @Query("SELECT SUM(g.soTien) FROM GiaoDich g " +
                "WHERE g.loaiGiaoDich = :loaiGD " +
                "AND g.ngayThucHien BETWEEN :startDate AND :endDate")
        BigDecimal sumSoTienByLoaiGiaoDichAndNgayThucHienBetween(
                @Param("loaiGD") TransactionType loaiGD,
                @Param("startDate") LocalDate startDate,
                @Param("endDate") LocalDate endDate
        );


        List<GiaoDich> findByMoSoTietKiemOrderByNgayThucHienDesc(MoSoTietKiem moSoTietKiem);

        @Query("SELECT g FROM GiaoDich g WHERE g.moSoTietKiem.nguoiDung.maND = :maND ORDER BY g.ngayThucHien DESC, g.id DESC")
        List<GiaoDich> findByMoSoTietKiem_NguoiDung_MaNDOrderByNgayThucHienDesc(
                @Param("maND") Integer maND,
                Pageable pageable
        );
        
        List<GiaoDich> findByMoSoTietKiem_NguoiDung_MaND(Integer maND);

        @Modifying
        void deleteByMoSoTietKiem(MoSoTietKiem moSoTietKiem);

        @Query("SELECT g FROM GiaoDich g WHERE g.id = :giaoDichId AND g.moSoTietKiem.nguoiDung.maND = :maND")
        Optional<GiaoDich> findByIdAndMoSoTietKiem_NguoiDung_MaND(
                @Param("giaoDichId") Long giaoDichId,
                @Param("maND") Integer maND
        );

        // Lấy tổng tiền giao dịch toàn hệ thống trong khoảng thời gian (dùng cho thống kê hệ thống)
        @Query("SELECT SUM(g.soTien) FROM GiaoDich g " +
                "WHERE g.loaiGiaoDich = :loaiGD " +
                "AND g.ngayThucHien BETWEEN :startDate AND :endDate")
        BigDecimal sumSoTienByLoaiGiaoDichAndDateRange(
                @Param("loaiGD") TransactionType loaiGD,
                @Param("startDate") LocalDate startDate,
                @Param("endDate") LocalDate endDate
        );

}