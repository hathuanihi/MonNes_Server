package com.SE104.quan_ly_so_tiet_kiem.controllers;

import com.SE104.quan_ly_so_tiet_kiem.repository.LoaiSoTietKiemDanhMucRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/loaisotietkiem-danhmuc")
public class LoaiSoTietKiemDanhMucController {
    @Autowired
    private LoaiSoTietKiemDanhMucRepository danhMucRepository;

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateTenLoaiDanhMuc(@PathVariable("id") Integer id, @RequestParam("ten") String ten) {
        return danhMucRepository.findById(id)
            .map(entity -> {
                entity.setTenLoaiDanhMuc(ten);
                danhMucRepository.save(entity);
                return ResponseEntity.ok("Đã cập nhật thành công");
            })
            .orElse(ResponseEntity.notFound().build());
    }
}
