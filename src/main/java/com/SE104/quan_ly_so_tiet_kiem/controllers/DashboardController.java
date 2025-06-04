package com.SE104.quan_ly_so_tiet_kiem.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
// import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.SE104.quan_ly_so_tiet_kiem.dto.DashboardSummaryDTO; 
import com.SE104.quan_ly_so_tiet_kiem.security.CustomUserDetails;
import com.SE104.quan_ly_so_tiet_kiem.service.DashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "User - Monthly Dashboard Summary", description = "API cho User xem thống kê nhanh theo tháng trên dashboard")
@RestController
@RequestMapping("/api/user/dashboard") 
// @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private DashboardService dashboardService;

    @Operation(summary = "Lấy thống kê nhanh theo tháng cho người dùng hiện tại (tổng dư, nạp/rút tháng này)")
    @GetMapping("/monthly-summary") 
    public ResponseEntity<DashboardSummaryDTO> getMonthlySummary(@AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.debug("User {} request: Get monthly dashboard summary", userDetails.getUsername());
        DashboardSummaryDTO summary = dashboardService.getMonthlySummaryForUser(userDetails.getUsername());
        return ResponseEntity.ok(summary);
    }
}