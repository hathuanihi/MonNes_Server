package com.SE104.quan_ly_so_tiet_kiem.repository;

import com.SE104.quan_ly_so_tiet_kiem.entity.GiaoDich;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface TransactionRepository extends JpaRepository<GiaoDich, Long> {

    @Query("SELECT COALESCE(SUM(g.soTien), 0) " +
           "FROM GiaoDich g " +
           "WHERE g.moSoTietKiem.nguoiDung.email = :email " +
           "AND g.loaiGiaoDich = :type " +
           "AND g.ngayThucHien BETWEEN :start AND :end")
    BigDecimal sumByTypeAndDateRange(
        @Param("email") String email,
        @Param("type") com.SE104.quan_ly_so_tiet_kiem.model.TransactionType type,
        @Param("start") LocalDate start,
        @Param("end") LocalDate end
    );

    @Query("SELECT COALESCE(SUM(CASE WHEN g.loaiGiaoDich = 'GUI_THEM' THEN g.soTien ELSE -g.soTien END), 0) " +
           "FROM GiaoDich g " +
           "WHERE g.moSoTietKiem.nguoiDung.email = :email")
    BigDecimal getWalletBalanceByEmail(@Param("email") String email);
}
