package com.SE104.quan_ly_so_tiet_kiem.controllers;

import com.SE104.quan_ly_so_tiet_kiem.dto.GiaoDichDTO;
import com.SE104.quan_ly_so_tiet_kiem.security.CustomUserDetails;
import com.SE104.quan_ly_so_tiet_kiem.service.GiaoDichService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "User - Transaction History", description = "API cho User xem lịch sử giao dịch")
@RestController
@RequestMapping("/api/user/transactions")
public class GiaoDichController {

    private static final Logger logger = LoggerFactory.getLogger(GiaoDichController.class);

    @Autowired
    private GiaoDichService giaoDichService;

    @Operation(summary = "Lấy danh sách các giao dịch gần đây của người dùng hiện tại (mặc định 10 giao dịch)")
    @GetMapping("/recent")
    public ResponseEntity<List<GiaoDichDTO>> getMyRecentTransactions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "10") int limit) {
        logger.debug("User {} request: Get recent transactions with limit {}", userDetails.getUsername(), limit);
        if (limit <= 0 || limit > 100) limit = 10; 
        return ResponseEntity.ok(giaoDichService.getRecentTransactionsForUser(userDetails.getMaND(), limit));
    }

    @Operation(summary = "Lấy chi tiết một giao dịch theo ID của người dùng hiện tại")
    @GetMapping("/{transactionId}")
    public ResponseEntity<GiaoDichDTO> getMyTransactionDetails(
            @PathVariable Long transactionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.debug("User {} request: Get transaction details for transactionId: {}", userDetails.getUsername(), transactionId);
        return ResponseEntity.ok(giaoDichService.getTransactionDetailsByUser(transactionId, userDetails.getMaND()));
    }
}