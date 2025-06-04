package com.SE104.quan_ly_so_tiet_kiem.repository;

import com.SE104.quan_ly_so_tiet_kiem.entity.GiaoDich;
import com.SE104.quan_ly_so_tiet_kiem.entity.MoSoTietKiem;
import com.SE104.quan_ly_so_tiet_kiem.model.TransactionType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional; 

@Repository
public interface GiaoDichRepository extends JpaRepository<GiaoDich, Long> {

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
}