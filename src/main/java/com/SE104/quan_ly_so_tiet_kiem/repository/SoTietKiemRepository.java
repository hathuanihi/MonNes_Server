package com.SE104.quan_ly_so_tiet_kiem.repository;

import com.SE104.quan_ly_so_tiet_kiem.entity.SoTietKiem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SoTietKiemRepository extends JpaRepository<SoTietKiem, Integer> {
    Optional<SoTietKiem> findByTenSo(String tenSo);

    boolean existsByTenSo(String tenSo);
}