package com.SE104.quan_ly_so_tiet_kiem.controllers;

import com.SE104.quan_ly_so_tiet_kiem.dto.MessageResponse;
import com.SE104.quan_ly_so_tiet_kiem.dto.SoTietKiemDTO;
import com.SE104.quan_ly_so_tiet_kiem.dto.SoTietKiemRequest;
import com.SE104.quan_ly_so_tiet_kiem.security.CustomUserDetails;
import com.SE104.quan_ly_so_tiet_kiem.service.SoTietKiemService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin - Savings Product Management", description = "API cho Admin quản lý các loại sản phẩm sổ tiết kiệm")
@RestController
@RequestMapping("/api/admin/savings-products-management") 
// @PreAuthorize("hasRole('ADMIN')") 
public class SoTietKiemController { 

    private static final Logger logger = LoggerFactory.getLogger(SoTietKiemController.class);

    @Autowired
    private SoTietKiemService soTietKiemService;

    @Operation(summary = "Admin lấy danh sách tất cả sản phẩm sổ tiết kiệm")
    @GetMapping
    public ResponseEntity<List<SoTietKiemDTO>> getAllSavingsProducts() {
        logger.debug("Admin request: Get all savings products via SoTietKiemController");
        return ResponseEntity.ok(soTietKiemService.getAllSoTietKiemDTOs());
    }

    @Operation(summary = "Admin lấy chi tiết một sản phẩm sổ tiết kiệm theo ID")
    @GetMapping("/{productId}")
    public ResponseEntity<SoTietKiemDTO> getSavingsProductById(@PathVariable Integer productId) {
        logger.debug("Admin request: Get savings product by ID {} via SoTietKiemController", productId);
        return ResponseEntity.ok(soTietKiemService.getSoTietKiemDTOById(productId));
    }


    @Operation(summary = "Admin tạo một sản phẩm sổ tiết kiệm mới")
    @PostMapping
    public ResponseEntity<SoTietKiemDTO> createSavingsProduct(
            @Valid @RequestBody SoTietKiemRequest request,
            @AuthenticationPrincipal CustomUserDetails adminPrincipal) {
        logger.debug("Admin request: Create new savings product by admin: {} via SoTietKiemController", adminPrincipal.getUsername());
        SoTietKiemDTO createdProduct = soTietKiemService.createSoTietKiem(request, adminPrincipal.getMaND());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @Operation(summary = "Admin cập nhật một sản phẩm sổ tiết kiệm")
    @PutMapping("/{productId}")
    public ResponseEntity<SoTietKiemDTO> updateSavingsProduct(
            @PathVariable Integer productId,
            @Valid @RequestBody SoTietKiemRequest request,
            @AuthenticationPrincipal CustomUserDetails adminPrincipal) {
        logger.debug("Admin request: Update savings product ID {} by admin: {} via SoTietKiemController", productId, adminPrincipal.getUsername());
        SoTietKiemDTO updatedProduct = soTietKiemService.updateSoTietKiem(productId, request, adminPrincipal.getMaND());
        return ResponseEntity.ok(updatedProduct);
    }

    @Operation(summary = "Admin xóa một sản phẩm sổ tiết kiệm")
    @DeleteMapping("/{productId}")
    public ResponseEntity<MessageResponse> deleteSavingsProduct(
            @PathVariable Integer productId,
            @AuthenticationPrincipal CustomUserDetails adminPrincipal) {
        logger.debug("Admin request: Delete savings product ID {} by admin: {} via SoTietKiemController", productId, adminPrincipal.getUsername());
        soTietKiemService.deleteSoTietKiem(productId, adminPrincipal.getMaND());
        return ResponseEntity.ok(new MessageResponse("Sản phẩm sổ tiết kiệm ID " + productId + " đã được xóa."));
    }
}