package com.SE104.quan_ly_so_tiet_kiem.repository;

import com.SE104.quan_ly_so_tiet_kiem.entity.MoSoTietKiem;
import com.SE104.quan_ly_so_tiet_kiem.entity.PhieuRutTien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhieuRutTienRepository extends JpaRepository<PhieuRutTien, Integer> {

    List<PhieuRutTien> findByMoSoTietKiem(MoSoTietKiem moSoTietKiem);

    @Modifying
    void deleteByMoSoTietKiem(MoSoTietKiem moSoTietKiem);
}