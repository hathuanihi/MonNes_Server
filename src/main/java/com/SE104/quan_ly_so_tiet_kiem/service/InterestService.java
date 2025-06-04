package com.SE104.quan_ly_so_tiet_kiem.service;

import com.SE104.quan_ly_so_tiet_kiem.entity.MoSoTietKiem;
import com.SE104.quan_ly_so_tiet_kiem.entity.PhieuGuiTien;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class InterestService {

    private static final int DEFAULT_SCALE = 4; 
    private static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final BigDecimal DAYS_IN_YEAR = new BigDecimal("365.0"); 
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100.0");

    public BigDecimal tinhTienLaiDuKien1Nam(MoSoTietKiem moSo) {
        if (moSo == null || moSo.getSoDu() == null || moSo.getLaiSuatApDung() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal soDu = moSo.getSoDu();
        BigDecimal laiSuatHangNamDaApDung = moSo.getLaiSuatApDung();

        if (laiSuatHangNamDaApDung.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;

        BigDecimal laiSuatHeSo = laiSuatHangNamDaApDung.divide(ONE_HUNDRED, DEFAULT_SCALE + 4, DEFAULT_ROUNDING_MODE); 
        return soDu.multiply(laiSuatHeSo).setScale(2, DEFAULT_ROUNDING_MODE); 
    }

    public BigDecimal tinhLaiTichLuyDon(MoSoTietKiem moSoTietKiem, LocalDate tinhDenNgay, BigDecimal laiSuatDeTinhTheoNam, BigDecimal principal) {
        if (moSoTietKiem == null || principal == null || principal.compareTo(BigDecimal.ZERO) < 0 ||
            laiSuatDeTinhTheoNam == null || laiSuatDeTinhTheoNam.compareTo(BigDecimal.ZERO) < 0 ) { 
            return BigDecimal.ZERO;
        }
         if (principal.compareTo(BigDecimal.ZERO) == 0 || laiSuatDeTinhTheoNam.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }


        LocalDate ngayMo = moSoTietKiem.getNgayMo();
        if (tinhDenNgay.isBefore(ngayMo) || tinhDenNgay.isEqual(ngayMo)) {
            return BigDecimal.ZERO;
        }

        long soNgayGui = ChronoUnit.DAYS.between(ngayMo, tinhDenNgay);
        if (soNgayGui <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal laiSuatHeSoHangNam = laiSuatDeTinhTheoNam.divide(ONE_HUNDRED, DEFAULT_SCALE + 6, DEFAULT_ROUNDING_MODE); 

        BigDecimal tienLai = principal
                .multiply(laiSuatHeSoHangNam)
                .multiply(new BigDecimal(soNgayGui))
                .divide(DAYS_IN_YEAR, DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);

        return tienLai.setScale(2, DEFAULT_ROUNDING_MODE); 
    }
    
    public BigDecimal tinhTongTienGocDaGui(MoSoTietKiem moSoTietKiem) {
        if (moSoTietKiem == null || moSoTietKiem.getPhieuGuiTienList() == null || moSoTietKiem.getPhieuGuiTienList().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return moSoTietKiem.getPhieuGuiTienList().stream()
                            .map(PhieuGuiTien::getSoTienGui)
                            .filter(java.util.Objects::nonNull) 
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}