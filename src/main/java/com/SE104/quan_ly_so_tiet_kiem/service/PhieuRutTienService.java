package com.SE104.quan_ly_so_tiet_kiem.service;

import com.SE104.quan_ly_so_tiet_kiem.entity.MoSoTietKiem;
import com.SE104.quan_ly_so_tiet_kiem.entity.PhieuRutTien;
import com.SE104.quan_ly_so_tiet_kiem.entity.SoTietKiem;
import com.SE104.quan_ly_so_tiet_kiem.model.TransactionType;
import com.SE104.quan_ly_so_tiet_kiem.repository.MoSoTietKiemRepository;
import com.SE104.quan_ly_so_tiet_kiem.repository.PhieuRutTienRepository;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; 

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date; 

@Service
public class PhieuRutTienService {
    private static final Logger logger = LoggerFactory.getLogger(PhieuRutTienService.class);

    private final PhieuRutTienRepository phieuRutTienRepository;
    private final MoSoTietKiemRepository moSoTietKiemRepository;
    private final GiaoDichService giaoDichService;
    private final MoSoTietKiemService moSoTietKiemService;
    private final InterestService interestService;

    private final Clock clock;

    private static final BigDecimal LAI_SUAT_SAU_DAO_HAN_DEFAULT = new BigDecimal("0.5"); 

    @Autowired
    public PhieuRutTienService(Clock clock, 
                               PhieuRutTienRepository phieuRutTienRepository,
                               MoSoTietKiemRepository moSoTietKiemRepository,
                               GiaoDichService giaoDichService,
                               MoSoTietKiemService moSoTietKiemService,
                               InterestService interestService) {
        this.phieuRutTienRepository = phieuRutTienRepository;
        this.moSoTietKiemRepository = moSoTietKiemRepository;
        this.giaoDichService = giaoDichService;
        this.moSoTietKiemService = moSoTietKiemService;
        this.interestService = interestService;
        this.clock = clock;
    }

    @Transactional 
    public PhieuRutTien withdraw(Integer moSoTietKiemId, BigDecimal soTienRut, Integer userId) {
        logger.info("User ID {} attempting to withdraw {} VND from account ID: {}", userId, soTienRut, moSoTietKiemId);
        
        // Validate user access and get account
        MoSoTietKiem moSoTietKiem = moSoTietKiemService.validateUserAccessAndGetAccount(moSoTietKiemId, userId);
        
        // Log current account details for debugging
        logger.debug("Account details - ID: {}, Status: {}, Balance: {}, Product Type: {}", 
            moSoTietKiem.getMaMoSo(), 
            moSoTietKiem.getTrangThai(), 
            moSoTietKiem.getSoDu(), 
            moSoTietKiem.getSoTietKiemSanPham() != null ? moSoTietKiem.getSoTietKiemSanPham().getTenSo() : "Unknown");
        
        SoTietKiem sanPham = moSoTietKiem.getSoTietKiemSanPham();
        if (sanPham == null) {
            throw new IllegalStateException("Lỗi hệ thống: Thông tin sản phẩm sổ tiết kiệm không hợp lệ (ID: " + moSoTietKiemId + ")");
        }

        LocalDate ngayRutTien = LocalDate.now(this.clock);
        
        // Basic validation
        validateWithdrawalAmount(soTienRut);

        // Calculate interest and validate based on account type
        WithdrawalCalculationResult result = calculateInterestAndValidate(moSoTietKiem, sanPham, soTienRut, ngayRutTien);

        // Create withdrawal slip
        PhieuRutTien phieuRutTien = createWithdrawalSlip(moSoTietKiem, soTienRut, ngayRutTien, result);
        PhieuRutTien savedPhieu = phieuRutTienRepository.save(phieuRutTien);

        // Update account balance and status
        updateAccountAfterWithdrawal(moSoTietKiem, soTienRut, ngayRutTien);

        // Save transaction
        giaoDichService.saveTransaction(soTienRut, TransactionType.WITHDRAW, moSoTietKiem, ngayRutTien);

        logger.info("User ID {} successfully withdrew {} VND (Interest: {}) from account ID {}. New balance: {}", 
                    userId, soTienRut, result.getTienLaiThucNhan(), moSoTietKiemId, moSoTietKiem.getSoDu());
        
        return savedPhieu;
    }

    /**
     * Calculate interest and validate withdrawal based on account type
     */
    private WithdrawalCalculationResult calculateInterestAndValidate(MoSoTietKiem moSoTietKiem, 
                                                                   SoTietKiem sanPham, 
                                                                   BigDecimal soTienRut, 
                                                                   LocalDate ngayRutTien) {
        BigDecimal soDuHienTai = moSoTietKiem.getSoDu();
        BigDecimal tienLaiThucNhan;
        BigDecimal laiSuatTinhKhiRut;
        
        if (sanPham.getKyHan() != null && sanPham.getKyHan() > 0) {
            // Term deposit
            validateTermDeposit(moSoTietKiem, sanPham, soTienRut, ngayRutTien);
            
            LocalDate ngayDaoHan = moSoTietKiem.getNgayDaoHan();
            
            if (ngayRutTien.isEqual(ngayDaoHan)) {
                // Withdraw on maturity date - use term interest rate
                LocalDate ngayBatDauTinhLai = determineInterestStartDate(moSoTietKiem, false);
                tienLaiThucNhan = calculateInterestSafely(soDuHienTai, moSoTietKiem.getLaiSuatApDung(), 
                                                        ngayBatDauTinhLai, ngayRutTien, moSoTietKiem.getMaMoSo());
                laiSuatTinhKhiRut = moSoTietKiem.getLaiSuatApDung();
                logger.info("Term Account ID {}: Withdrawing on maturity date with term rate ({}%)", 
                           moSoTietKiem.getMaMoSo(), moSoTietKiem.getLaiSuatApDung());
            } else {
                // Withdraw after maturity date - use non-term interest rate
                LocalDate ngayBatDauTinhLai = determineInterestStartDate(moSoTietKiem, true);
                tienLaiThucNhan = calculateInterestSafely(soDuHienTai, LAI_SUAT_SAU_DAO_HAN_DEFAULT, 
                                                        ngayBatDauTinhLai, ngayRutTien, moSoTietKiem.getMaMoSo());
                laiSuatTinhKhiRut = LAI_SUAT_SAU_DAO_HAN_DEFAULT;
                logger.info("Term Account ID {}: Withdrawing after maturity date with non-term rate ({}%)", 
                           moSoTietKiem.getMaMoSo(), LAI_SUAT_SAU_DAO_HAN_DEFAULT);
            }
        } else {
            // Non-term deposit
            validateNonTermDeposit(moSoTietKiem, sanPham, soTienRut, ngayRutTien);
            
            LocalDate ngayBatDauTinhLai = determineInterestStartDate(moSoTietKiem, false);
            tienLaiThucNhan = calculateInterestSafely(soDuHienTai, moSoTietKiem.getLaiSuatApDung(), 
                                                    ngayBatDauTinhLai, ngayRutTien, moSoTietKiem.getMaMoSo());
            laiSuatTinhKhiRut = moSoTietKiem.getLaiSuatApDung();
            logger.info("Non-term Account ID {}: Calculating interest with rate ({}%)", 
                       moSoTietKiem.getMaMoSo(), moSoTietKiem.getLaiSuatApDung());
        }
        
        return new WithdrawalCalculationResult(tienLaiThucNhan, laiSuatTinhKhiRut);
    }

    /**
     * Determine the start date for interest calculation
     */
    private LocalDate determineInterestStartDate(MoSoTietKiem moSoTietKiem, boolean isOverdue) {
        if (isOverdue && moSoTietKiem.getNgayDaoHan() != null) {
            // For overdue term deposits, interest starts from maturity date or last interest payment date
            LocalDate ngayTraLaiCuoi = moSoTietKiem.getNgayTraLaiCuoiCung();
            LocalDate ngayDaoHan = moSoTietKiem.getNgayDaoHan();
            
            if (ngayTraLaiCuoi != null && ngayTraLaiCuoi.isAfter(ngayDaoHan)) {
                return ngayTraLaiCuoi;
            } else {
                return ngayDaoHan;
            }
        } else {
            // Normal case: start from last interest payment date or account opening date
            return moSoTietKiem.getNgayTraLaiCuoiCung() != null ? 
                   moSoTietKiem.getNgayTraLaiCuoiCung() : moSoTietKiem.getNgayMo();
        }
    }

    /**
     * Calculate interest with proper error handling and validation
     */
    private BigDecimal calculateInterestSafely(BigDecimal principal, BigDecimal interestRate, 
                                             LocalDate fromDate, LocalDate toDate, Integer accountId) {
        try {
            // Validate dates
            if (fromDate == null || toDate == null) {
                logger.warn("Account ID {}: Invalid dates for interest calculation: from={}, to={}", 
                           accountId, fromDate, toDate);
                return BigDecimal.ZERO;
            }
            
            if (fromDate.isAfter(toDate)) {
                logger.warn("Account ID {}: From date ({}) is after to date ({}). No interest accrued.", 
                           accountId, fromDate, toDate);
                return BigDecimal.ZERO;
            }
            
            if (fromDate.isEqual(toDate)) {
                logger.info("Account ID {}: Same from and to date ({}). No interest accrued.", 
                           accountId, fromDate);
                return BigDecimal.ZERO;
            }
            
            return interestService.tinhLaiDonTheoThoiGian(principal, interestRate, fromDate, toDate);
            
        } catch (Exception e) {
            logger.error("Account ID {}: Error calculating interest from {} to {} with rate {}%: {}", 
                        accountId, fromDate, toDate, interestRate, e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * Create withdrawal slip entity
     */
    private PhieuRutTien createWithdrawalSlip(MoSoTietKiem moSoTietKiem, 
                                            BigDecimal soTienRut, 
                                            LocalDate ngayRutTien, 
                                            WithdrawalCalculationResult result) {
        PhieuRutTien phieuRutTien = new PhieuRutTien();
        phieuRutTien.setMoSoTietKiem(moSoTietKiem);
        phieuRutTien.setSoTienRut(soTienRut); 
        phieuRutTien.setNgayRut(Date.from(ngayRutTien.atStartOfDay(clock.getZone()).toInstant())); 
        phieuRutTien.setLaiSuatKhiRut(result.getLaiSuatTinhKhiRut());
        phieuRutTien.setTienLaiThucNhan(result.getTienLaiThucNhan().setScale(2, RoundingMode.HALF_UP));
        return phieuRutTien;
    }

    /**
     * Update account balance and status after withdrawal
     */
    private void updateAccountAfterWithdrawal(MoSoTietKiem moSoTietKiem, 
                                            BigDecimal soTienRut, 
                                            LocalDate ngayRutTien) {
        BigDecimal soDuMoi = moSoTietKiem.getSoDu().subtract(soTienRut);
        moSoTietKiem.setSoDu(soDuMoi);
        
        // Update last interest payment date to withdrawal date
        moSoTietKiem.setNgayTraLaiCuoiCung(ngayRutTien);
        
        // Close account if balance is zero
        if (soDuMoi.compareTo(BigDecimal.ZERO) == 0) {
            moSoTietKiem.setTrangThai(MoSoTietKiem.TrangThaiMoSo.DA_DONG);
            logger.info("Account ID {} closed due to zero balance after withdrawal", moSoTietKiem.getMaMoSo());
        }
        
        moSoTietKiemRepository.save(moSoTietKiem);
    }

    /**
     * Validate withdrawal amount
     */
    private void validateWithdrawalAmount(BigDecimal soTienRut) {
        if (soTienRut == null || soTienRut.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền rút phải lớn hơn 0");
        }
        
        // Minimum withdrawal amount (example: 10,000 VND)
        BigDecimal minWithdrawal = new BigDecimal("10000");
        if (soTienRut.compareTo(minWithdrawal) < 0) {
            throw new IllegalArgumentException("Số tiền rút tối thiểu là " + minWithdrawal.toPlainString() + " VND");
        }
    }

    /**
     * Validate term deposit withdrawal
     */
    private void validateTermDeposit(MoSoTietKiem moSoTietKiem, SoTietKiem sanPham, 
                                   BigDecimal soTienRut, LocalDate ngayRutTien) {
        // Check account status - only reject closed accounts, allow active and matured accounts
        if (moSoTietKiem.getTrangThai() == MoSoTietKiem.TrangThaiMoSo.DA_DONG) {
            throw new IllegalStateException("Không thể rút tiền từ tài khoản đã đóng (ID: " + moSoTietKiem.getMaMoSo() + ")");
        }

        // Check sufficient balance
        if (moSoTietKiem.getSoDu().compareTo(soTienRut) < 0) {
            throw new IllegalArgumentException("Số dư không đủ để thực hiện giao dịch. Số dư hiện tại: " + 
                                             moSoTietKiem.getSoDu().toPlainString() + " VND");
        }

        // Term deposit specific validations
        LocalDate ngayMo = moSoTietKiem.getNgayMo();
        LocalDate ngayDaoHan = moSoTietKiem.getNgayDaoHan();
        
        if (ngayDaoHan == null) {
            throw new IllegalStateException("Ngày đáo hạn không hợp lệ cho sổ tiết kiệm có kỳ hạn (ID: " + moSoTietKiem.getMaMoSo() + ")");
        }

        // Check if withdrawal is before maturity and not allowed
        if (ngayRutTien.isBefore(ngayDaoHan)) {
            // Check if early withdrawal is allowed for this product type
            // For now, we'll allow early withdrawal but with different interest calculation
            logger.warn("Early withdrawal detected for term account ID {}: withdrawal date {} is before maturity date {}", 
                       moSoTietKiem.getMaMoSo(), ngayRutTien, ngayDaoHan);
        }

        logger.info("Term deposit validation passed for account ID {}", moSoTietKiem.getMaMoSo());
    }

    /**
     * Validate non-term deposit withdrawal
     */
    private void validateNonTermDeposit(MoSoTietKiem moSoTietKiem, SoTietKiem sanPham, 
                                      BigDecimal soTienRut, LocalDate ngayRutTien) {
        // Check account status - only reject closed accounts, allow active and matured accounts  
        if (moSoTietKiem.getTrangThai() == MoSoTietKiem.TrangThaiMoSo.DA_DONG) {
            throw new IllegalStateException("Không thể rút tiền từ tài khoản đã đóng (ID: " + moSoTietKiem.getMaMoSo() + ")");
        }

        // Check sufficient balance
        if (moSoTietKiem.getSoDu().compareTo(soTienRut) < 0) {
            throw new IllegalArgumentException("Số dư không đủ để thực hiện giao dịch. Số dư hiện tại: " + 
                                             moSoTietKiem.getSoDu().toPlainString() + " VND");
        }

        // Non-term deposit specific validations
        LocalDate ngayMo = moSoTietKiem.getNgayMo();
        
        // Check if account has been open for minimum period (e.g., 1 day)
        if (ngayRutTien.isBefore(ngayMo.plusDays(1))) {
            throw new IllegalArgumentException("Không thể rút tiền trong ngày đầu mở sổ tiết kiệm");
        }

        logger.info("Non-term deposit validation passed for account ID {}", moSoTietKiem.getMaMoSo());
    }

    /**
     * Inner class to hold withdrawal calculation results
     */
    private static class WithdrawalCalculationResult {
        private final BigDecimal tienLaiThucNhan;
        private final BigDecimal laiSuatTinhKhiRut;
        
        public WithdrawalCalculationResult(BigDecimal tienLaiThucNhan, BigDecimal laiSuatTinhKhiRut) {
            this.tienLaiThucNhan = tienLaiThucNhan;
            this.laiSuatTinhKhiRut = laiSuatTinhKhiRut;
        }
        
        public BigDecimal getTienLaiThucNhan() {
            return tienLaiThucNhan;
        }
        
        public BigDecimal getLaiSuatTinhKhiRut() {
            return laiSuatTinhKhiRut;
        }
    }
}