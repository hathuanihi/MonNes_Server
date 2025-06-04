package com.SE104.quan_ly_so_tiet_kiem.repository;

import com.SE104.quan_ly_so_tiet_kiem.entity.DangNhap;
import com.SE104.quan_ly_so_tiet_kiem.entity.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface DangNhapRepository extends JpaRepository<DangNhap, Integer> {

    Long countByLoginTimeBetween(Date from, Date to);

    @Modifying 
    @Query("DELETE FROM DangNhap d WHERE d.nguoiDung = :nguoiDung")
    void deleteByNguoiDung(@Param("nguoiDung") NguoiDung nguoiDung);

    List<DangNhap> findByNguoiDung(NguoiDung nguoiDung);
}