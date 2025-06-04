package com.SE104.quan_ly_so_tiet_kiem.controllers;

import com.SE104.quan_ly_so_tiet_kiem.dto.*;
import com.SE104.quan_ly_so_tiet_kiem.entity.PhieuGuiTien; 
import com.SE104.quan_ly_so_tiet_kiem.entity.PhieuRutTien;  
import com.SE104.quan_ly_so_tiet_kiem.security.CustomUserDetails;
import com.SE104.quan_ly_so_tiet_kiem.service.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
// import org.springframework.security.access.prepost.PreAuthorize; 
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "User - Savings Account Management", description = "API cho User quản lý các sổ tiết kiệm của họ")
@RestController
@RequestMapping("/api/user/savings")
// @PreAuthorize("hasAnyRole('USER', 'ADMIN')") 
public class UserSavingsController {

    private static final Logger logger = LoggerFactory.getLogger(UserSavingsController.class);

    @Autowired
    private MoSoTietKiemService moSoTietKiemService;
    @Autowired
    private PhieuGuiTienService phieuGuiTienService;
    @Autowired
    private PhieuRutTienService phieuRutTienService;
    @Autowired
    private GiaoDichService giaoDichService; 
    @Autowired
    private UserService userService; 

    @Operation(summary = "Lấy tất cả các sổ tiết kiệm đã mở của người dùng hiện tại")
    @GetMapping
    public ResponseEntity<List<MoSoTietKiemResponse>> getAllUserSavings(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.debug("User {} request: Get all their savings accounts", userDetails.getUsername());
        return ResponseEntity.ok(moSoTietKiemService.getUserSavingsAccounts(userDetails.getMaND()));
    }

    @Operation(summary = "Lấy chi tiết một sổ tiết kiệm theo ID của người dùng hiện tại")
    @GetMapping("/{moSoId}")
    public ResponseEntity<MoSoTietKiemResponse> getSavingsDetails(
            @PathVariable Integer moSoId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.debug("User {} request: Get savings account details for moSoId: {}", userDetails.getUsername(), moSoId);
        return ResponseEntity.ok(moSoTietKiemService.getSavingsAccountDetails(moSoId, userDetails.getMaND()));
    }

    @Operation(summary = "Tạo một sổ tiết kiệm mới cho người dùng hiện tại")
    @PostMapping
    public ResponseEntity<MoSoTietKiemResponse> createSavings(
            @Valid @RequestBody MoSoTietKiemRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.info("User {} request: Create new savings account with product ID: {} and initial amount: {}",
                userDetails.getUsername(), request.getSoTietKiemSanPhamId(), request.getSoTienGuiBanDau());
        MoSoTietKiemResponse createdAccount = moSoTietKiemService.createSavingsAccount(request, userDetails.getMaND());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
    }

    @Operation(summary = "Gửi tiền vào một sổ tiết kiệm của người dùng hiện tại")
    @PostMapping("/{moSoId}/deposit")
    public ResponseEntity<PhieuGuiTien> deposit(
            @PathVariable Integer moSoId,
            @Valid @RequestBody DepositRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.info("User {} request: Deposit {} into moSoId: {}", userDetails.getUsername(), request.getSoTien(), moSoId);
        PhieuGuiTien phieuGuiTien = phieuGuiTienService.deposit(moSoId, request.getSoTien(), userDetails.getMaND(), false);
        return ResponseEntity.ok(phieuGuiTien);
    }

    @Operation(summary = "Rút tiền từ một sổ tiết kiệm của người dùng hiện tại")
    @PostMapping("/{moSoId}/withdraw")
    public ResponseEntity<PhieuRutTien> withdraw(
            @PathVariable Integer moSoId,
            @Valid @RequestBody WithdrawRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.info("User {} request: Withdraw {} from moSoId: {}", userDetails.getUsername(), request.getSoTien(), moSoId);
        PhieuRutTien phieuRutTien = phieuRutTienService.withdraw(moSoId, request.getSoTien(), userDetails.getMaND());
        return ResponseEntity.ok(phieuRutTien);
    }

    @Operation(summary = "Lấy danh sách tất cả giao dịch của một sổ tiết kiệm cụ thể")
    @GetMapping("/{moSoId}/transactions")
    public ResponseEntity<List<GiaoDichDTO>> getTransactionsForAccount(
            @PathVariable Integer moSoId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.debug("User {} request: Get transactions for moSoId: {}", userDetails.getUsername(), moSoId);
        return ResponseEntity.ok(giaoDichService.getTransactionsForMoSoTietKiem(moSoId, userDetails.getMaND()));
    }

    @Operation(summary = "Lấy tổng hợp thông tin tài khoản của người dùng hiện tại")
    @GetMapping("/account-summary") 
    public ResponseEntity<UserAccountSummaryDTO> getAccountSummary(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.debug("User {} request: Get account summary", userDetails.getUsername());
        return ResponseEntity.ok(userService.getUserAccountSummary(userDetails.getMaND()));
    }
}