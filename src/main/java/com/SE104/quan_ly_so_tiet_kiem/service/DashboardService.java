package com.SE104.quan_ly_so_tiet_kiem.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import org.springframework.beans.factory.annotation.Autowired;
import com.SE104.quan_ly_so_tiet_kiem.dto.DashboardSummaryDTO;
import com.SE104.quan_ly_so_tiet_kiem.entity.MoSoTietKiem;
import com.SE104.quan_ly_so_tiet_kiem.entity.NguoiDung;
import com.SE104.quan_ly_so_tiet_kiem.model.TransactionType;
import com.SE104.quan_ly_so_tiet_kiem.repository.GiaoDichRepository;
import com.SE104.quan_ly_so_tiet_kiem.repository.MoSoTietKiemRepository;
import com.SE104.quan_ly_so_tiet_kiem.repository.NguoiDungRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class DashboardService {

    @Autowired
    private GiaoDichRepository giaoDichRepository;
    @Autowired
    private NguoiDungRepository nguoiDungRepository;
    @Autowired
    private MoSoTietKiemRepository moSoTietKiemRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryDTO getMonthlySummaryForUser(String email) {
        NguoiDung nguoiDung = nguoiDungRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với email: " + email));

        BigDecimal currentTotalBalance = moSoTietKiemRepository.findByNguoiDung_MaNDAndTrangThai(nguoiDung.getMaND(), MoSoTietKiem.TrangThaiMoSo.DANG_HOAT_DONG)
                .stream()
                .map(MoSoTietKiem::getSoDu)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        YearMonth currentMonth = YearMonth.now();
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();

        BigDecimal depositThisMonth = giaoDichRepository.sumSoTienByNguoiDungAndLoaiGiaoDichAndDateRange(
                nguoiDung.getMaND(), 
                TransactionType.DEPOSIT,
                startDate,
                endDate);
        depositThisMonth = (depositThisMonth == null) ? BigDecimal.ZERO : depositThisMonth;

        BigDecimal withdrawThisMonth = giaoDichRepository.sumSoTienByNguoiDungAndLoaiGiaoDichAndDateRange(
                nguoiDung.getMaND(), 
                TransactionType.WITHDRAW,
                startDate,
                endDate);
        withdrawThisMonth = (withdrawThisMonth == null) ? BigDecimal.ZERO : withdrawThisMonth;
        

        Integer activeAccountsCount = moSoTietKiemRepository.countByNguoiDung_MaNDAndTrangThai(
            nguoiDung.getMaND(), 
            MoSoTietKiem.TrangThaiMoSo.DANG_HOAT_DONG
        );
        activeAccountsCount = (activeAccountsCount == null) ? 0 : activeAccountsCount;

        DashboardSummaryDTO summaryDTO = new DashboardSummaryDTO();
        summaryDTO.setTongSoDuTatCaSoCuaUser(currentTotalBalance);
        summaryDTO.setTongTienDaNapThangNay(depositThisMonth);
        summaryDTO.setTongTienDaRutThangNay(withdrawThisMonth);
        summaryDTO.setSoLuongSoTietKiemDangHoatDong(activeAccountsCount);
        
        return summaryDTO;
    }
}