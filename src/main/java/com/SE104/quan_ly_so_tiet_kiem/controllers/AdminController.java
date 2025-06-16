package com.SE104.quan_ly_so_tiet_kiem.controllers;

import com.SE104.quan_ly_so_tiet_kiem.dto.*;
import com.SE104.quan_ly_so_tiet_kiem.security.CustomUserDetails;
import com.SE104.quan_ly_so_tiet_kiem.service.AdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort; 
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin Management", description = "API cho Admin quản lý người dùng, loại sổ, giao dịch và thống kê")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private AdminService adminService;

    @Operation(summary = "Lấy danh sách tất cả người dùng cùng chi tiết tài khoản và tổng số dư")
    @GetMapping("/users")
    public ResponseEntity<List<UserDetailDTO>> getAllUsersWithDetails() {
        logger.debug("Admin request: Get all users with details");
        return ResponseEntity.ok(adminService.getAllUsersWithAccountDetails());
    }

    @Operation(summary = "Lấy chi tiết một người dùng theo ID, bao gồm tài khoản và tổng số dư")
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDetailDTO> getUserDetailsById(@PathVariable Integer userId) {
        logger.debug("Admin request: Get user details for userId: {}", userId);
        return ResponseEntity.ok(adminService.getUserDetailsByUserId(userId));
    }

    @Operation(summary = "Admin cập nhật thông tin cá nhân cho một người dùng")
    @PutMapping("/users/{userIdToUpdate}")
    public ResponseEntity<UserResponse> updateUserByAdmin(
            @PathVariable Integer userIdToUpdate,
            @Valid @RequestBody UpdateProfileDTO profileDTO,
            @AuthenticationPrincipal CustomUserDetails adminPrincipal) {
        logger.debug("Admin request: Update user profile for userId: {} by admin: {}", userIdToUpdate, adminPrincipal.getUsername());
        Integer adminId = adminPrincipal.getMaND();
        return ResponseEntity.ok(adminService.updateUserByAdmin(userIdToUpdate, profileDTO, adminId));
    }
    
    @Operation(summary = "Admin xóa một người dùng (USER role only)")
    @DeleteMapping("/users/{userIdToDelete}")
    public ResponseEntity<MessageResponse> deleteUserByAdmin(
            @PathVariable Integer userIdToDelete,
            @AuthenticationPrincipal CustomUserDetails adminPrincipal) {
        logger.debug("Admin request: Delete user with userId: {} by admin: {}", userIdToDelete, adminPrincipal.getUsername());
        adminService.deleteUserByAdmin(userIdToDelete, adminPrincipal.getMaND());
        return ResponseEntity.ok(new MessageResponse("Người dùng ID " + userIdToDelete + " đã được xóa thành công."));
    }

    /*
    @Operation(summary = "Admin tạo một sản phẩm sổ tiết kiệm mới")
    @PostMapping("/savings-products")
    public ResponseEntity<SoTietKiemDTO> createSavingsProductDirectly(
            @Valid @RequestBody SoTietKiemRequest request,
            @AuthenticationPrincipal CustomUserDetails adminPrincipal) {
        logger.debug("Admin request: Create new savings product by admin: {} via AdminController", adminPrincipal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminService.createSavingsProduct(request, adminPrincipal.getMaND()));
    }

    @Operation(summary = "Admin lấy danh sách tất cả sản phẩm sổ tiết kiệm")
    @GetMapping("/savings-products")
    public ResponseEntity<List<SoTietKiemDTO>> getAllSavingsProductsDirectly() {
        logger.debug("Admin request: Get all savings products via AdminController");
        return ResponseEntity.ok(soTietKiemService.getAllSoTietKiemDTOs()); 
    }
    */

    @Operation(summary = "Admin xem thống kê hệ thống (lượt truy cập, doanh thu,...)")
    @GetMapping("/statistics")
    public ResponseEntity<ThongKeDTO> getSystemStatistics() {
        logger.debug("Admin request: Get system statistics");
        return ResponseEntity.ok(adminService.getSystemStatistics());
    }    @Operation(summary = "Admin xem tất cả giao dịch trong hệ thống")
    @GetMapping("/transactions")
    public ResponseEntity<Page<GiaoDichDTO>> getAllSystemTransactions(
            @PageableDefault(size = 10, sort = "ngayThucHien", direction = Sort.Direction.DESC) Pageable pageable) {
        logger.debug("Admin request: Get all system transactions with pageable: {}", pageable);
        
        try {
            Page<GiaoDichDTO> result = adminService.getAllSystemTransactionsPaginated(pageable);
            logger.debug("Successfully retrieved {} transactions", result.getTotalElements());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error retrieving system transactions", e);
            throw new RuntimeException("Failed to retrieve system transactions: " + e.getMessage(), e);
        }
    }

    @GetMapping("/savings-categories")
    public ResponseEntity<List<LoaiSoTietKiemDanhMucDTO>> getAllSavingCategories() {
        logger.info("Admin request: Get all savings product categories");
        List<LoaiSoTietKiemDanhMucDTO> categories = adminService.getAllSavingCategories(); 
        
        if (categories == null || categories.isEmpty()) { 
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(categories);
    }
}