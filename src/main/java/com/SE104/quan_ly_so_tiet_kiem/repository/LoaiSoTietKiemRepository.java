package com.SE104.quan_ly_so_tiet_kiem.repository;

import com.SE104.quan_ly_so_tiet_kiem.entity.LoaiSoTietKiem; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoaiSoTietKiemRepository extends JpaRepository<LoaiSoTietKiem, Integer> {
    boolean existsByTenLoaiDanhMuc(String tenLoaiDanhMuc);
}