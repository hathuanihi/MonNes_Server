package com.SE104.quan_ly_so_tiet_kiem.repository;

import com.SE104.quan_ly_so_tiet_kiem.entity.MoSoTietKiem;
import com.SE104.quan_ly_so_tiet_kiem.entity.PhieuGuiTien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhieuGuiTienRepository extends JpaRepository<PhieuGuiTien, Integer> {

    List<PhieuGuiTien> findByMoSoTietKiem(MoSoTietKiem moSoTietKiem);

    @Modifying
    void deleteByMoSoTietKiem(MoSoTietKiem moSoTietKiem);
}