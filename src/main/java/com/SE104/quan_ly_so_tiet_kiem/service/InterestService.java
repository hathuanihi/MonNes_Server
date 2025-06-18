package com.SE104.quan_ly_so_tiet_kiem.service;

import com.SE104.quan_ly_so_tiet_kiem.entity.MoSoTietKiem;
import com.SE104.quan_ly_so_tiet_kiem.entity.PhieuGuiTien;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Service
public class InterestService {
    private static final Logger logger = LoggerFactory.getLogger(InterestService.class);
    private static final int DEFAULT_SCALE = 2;
    private static final int CALCULATION_SCALE = 10;
    private static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final BigDecimal DAYS_IN_YEAR = new BigDecimal("365");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");    public BigDecimal tinhLaiDonTheoNgay(BigDecimal principal, 
                                         BigDecimal laiSuatPhanTramNam, 
                                         LocalDate ngayBatDauTinhLai, 
                                         LocalDate tinhDenNgay) {
        // Delegate to the main method with enhanced validation
        return tinhLaiDonTheoThoiGian(principal, laiSuatPhanTramNam, ngayBatDauTinhLai, tinhDenNgay);
    }

    public BigDecimal tinhLaiTichLuyDon(MoSoTietKiem moSoTietKiem, 
                                         LocalDate tinhDenNgay, 
                                         BigDecimal laiSuatDeTinhTheoNam, 
                                         BigDecimal principal, 
                                         LocalDate ngayBatDauKyLai) {
        LocalDate actualNgayBatDau = ngayBatDauKyLai != null ? ngayBatDauKyLai : moSoTietKiem.getNgayMo();
        logger.debug("tinhLaiTichLuyDon: Account ID {}, principal={}, rate={}, from={}, to={}", 
                     moSoTietKiem.getMaMoSo(), principal, laiSuatDeTinhTheoNam, actualNgayBatDau, tinhDenNgay);
        return tinhLaiDonTheoNgay(principal, laiSuatDeTinhTheoNam, actualNgayBatDau, tinhDenNgay);
    }

    public BigDecimal tinhLaiTichLuyDon(MoSoTietKiem moSoTietKiem, 
                                        LocalDate tinhDenNgay, 
                                        BigDecimal laiSuatDeTinhTheoNam, 
                                        BigDecimal principal) {
        LocalDate ngayBatDauTinhLai = moSoTietKiem.getNgayMo();
        logger.debug("tinhLaiTichLuyDon (4 params): Account ID {}, principal={}, rate={}, from={}, to={}", 
                     moSoTietKiem.getMaMoSo(), principal, laiSuatDeTinhTheoNam, ngayBatDauTinhLai, tinhDenNgay);
        return tinhLaiDonTheoThoiGian(principal, laiSuatDeTinhTheoNam, ngayBatDauTinhLai, tinhDenNgay);
    }    public BigDecimal tinhLaiDonTheoThoiGian(BigDecimal principal, 
                                             BigDecimal laiSuatPhanTramNam, 
                                             LocalDate ngayBatDauTinhLai, 
                                             LocalDate tinhDenNgay) {
        // Validate input parameters
        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0) {
            logger.error("Invalid principal amount: {}", principal);
            throw new IllegalArgumentException("Số tiền gốc phải lớn hơn 0.");
        }
        
        if (laiSuatPhanTramNam == null || laiSuatPhanTramNam.compareTo(BigDecimal.ZERO) < 0) {
            logger.error("Invalid interest rate: {}", laiSuatPhanTramNam);
            throw new IllegalArgumentException("Lãi suất không được âm.");
        }
        
        if (ngayBatDauTinhLai == null || tinhDenNgay == null) {
            logger.error("Invalid dates: from={}, to={}", ngayBatDauTinhLai, tinhDenNgay);
            throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được null.");
        }
        
        // Handle edge cases for dates
        if (ngayBatDauTinhLai.isAfter(tinhDenNgay)) {
            logger.warn("Start date ({}) is after end date ({}). Returning ZERO interest.", 
                        ngayBatDauTinhLai, tinhDenNgay);
            return BigDecimal.ZERO;
        }
        
        if (ngayBatDauTinhLai.isEqual(tinhDenNgay)) {
            logger.info("Start date equals end date ({}). Returning ZERO interest.", ngayBatDauTinhLai);
            return BigDecimal.ZERO;
        }

        long soNgayTinhLai = ChronoUnit.DAYS.between(ngayBatDauTinhLai, tinhDenNgay);
        logger.debug("Calculating interest: soNgayTinhLai={} (from {} to {})", soNgayTinhLai, ngayBatDauTinhLai, tinhDenNgay);
        
        // Double-check for edge case (should not happen with above validation)
        if (soNgayTinhLai <= 0) {
            logger.warn("Calculated zero or negative days for interest: {} days. Returning ZERO interest.", soNgayTinhLai);
            return BigDecimal.ZERO;
        }
        
        // Handle zero interest rate
        if (laiSuatPhanTramNam.compareTo(BigDecimal.ZERO) == 0) {
            logger.info("Interest rate is 0%. No interest accrued.");
            return BigDecimal.ZERO;
        }

        try {
            BigDecimal laiSuatHeSoHangNam = laiSuatPhanTramNam.divide(ONE_HUNDRED, CALCULATION_SCALE + 4, DEFAULT_ROUNDING_MODE);
            BigDecimal tienLai = principal
                    .multiply(laiSuatHeSoHangNam)
                    .multiply(new BigDecimal(soNgayTinhLai))
                    .divide(DAYS_IN_YEAR, CALCULATION_SCALE, DEFAULT_ROUNDING_MODE);
            
            logger.debug("Calculated Interest: Principal={}, Rate={}, Days={}, InterestRaw={}, InterestRounded={}", 
                         principal, laiSuatPhanTramNam, soNgayTinhLai, tienLai, tienLai.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE));

            return tienLai.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
            
        } catch (ArithmeticException e) {
            logger.error("Arithmetic error in interest calculation: {}", e.getMessage(), e);
            throw new IllegalStateException("Lỗi tính toán lãi suất: " + e.getMessage());
        }
    }

    public BigDecimal tinhTongTienGocDaGui(MoSoTietKiem moSoTietKiem) {
        if (moSoTietKiem == null || moSoTietKiem.getPhieuGuiTienList() == null || moSoTietKiem.getPhieuGuiTienList().isEmpty()) {
            logger.info("No deposits found for Account ID {}. Returning ZERO.", moSoTietKiem != null ? moSoTietKiem.getMaMoSo() : "null");
            return BigDecimal.ZERO;
        }
        BigDecimal total = moSoTietKiem.getPhieuGuiTienList().stream()
                .map(PhieuGuiTien::getSoTienGui)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        logger.debug("Total deposits for Account ID {}: {}", moSoTietKiem.getMaMoSo(), total);
        return total;
    }
}