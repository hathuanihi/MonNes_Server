package com.SE104.quan_ly_so_tiet_kiem.controllers;

import com.SE104.quan_ly_so_tiet_kiem.dto.UpdateProfileDTO;
import com.SE104.quan_ly_so_tiet_kiem.dto.UserResponse;
import com.SE104.quan_ly_so_tiet_kiem.security.CustomUserDetails;
import com.SE104.quan_ly_so_tiet_kiem.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
// import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "User - Profile and Dashboard", description = "API cho User xem và cập nhật hồ sơ, xem dashboard")
@RestController
@RequestMapping("/api/user")
// @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class UserProfileController {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);

    @Autowired
    private UserService userService;

    @Operation(summary = "Lấy thông tin hồ sơ của người dùng hiện tại")
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getUserProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.debug("User {} request: Get profile", userDetails.getUsername());
        return ResponseEntity.ok(userService.getUserProfileById(userDetails.getMaND()));
    }

    @Operation(summary = "Cập nhật thông tin hồ sơ của người dùng hiện tại")
    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateUserProfile(
            @Valid @RequestBody UpdateProfileDTO updateProfileDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.info("User {} request: Update profile with data: {}", userDetails.getUsername(), updateProfileDTO);
        UserResponse updatedUser = userService.updateUserProfile(userDetails.getMaND(), updateProfileDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Lấy dữ liệu dashboard tổng quan cho người dùng hiện tại")
    @GetMapping("/dashboard-overview") 
    public ResponseEntity<Map<String, Object>> getUserDashboardOverview(@AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.debug("User {} request: Get dashboard overview", userDetails.getUsername());
        Map<String, Object> dashboardData = userService.getUserDashboardData(userDetails.getMaND());
        return ResponseEntity.ok(dashboardData);
    }
}