package com.SE104.quan_ly_so_tiet_kiem.repository;

import com.SE104.quan_ly_so_tiet_kiem.entity.NguoiDung;
import com.SE104.quan_ly_so_tiet_kiem.entity.SoTietKiem;
import com.SE104.quan_ly_so_tiet_kiem.entity.ThayDoi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThayDoiRepository extends JpaRepository<ThayDoi, Integer> {

    boolean existsBySoTietKiemSanPham_MaSo(Integer maSo); 

    List<ThayDoi> findBySoTietKiemSanPham_MaSo(Integer maSo);

    List<ThayDoi> findByNguoiDungAdmin(NguoiDung nguoiDungAdmin);

    List<ThayDoi> findBySoTietKiemSanPham(SoTietKiem soTietKiemSanPham);

    @Modifying
    void deleteByNguoiDungAdmin(NguoiDung nguoiDungAdmin);

    @Modifying
    void deleteBySoTietKiemSanPham(SoTietKiem soTietKiemSanPham);
}