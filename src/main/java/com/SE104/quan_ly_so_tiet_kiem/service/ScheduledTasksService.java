package com.SE104.quan_ly_so_tiet_kiem.service;

import com.SE104.quan_ly_so_tiet_kiem.dto.DashboardSummaryDTO;
import com.SE104.quan_ly_so_tiet_kiem.entity.MoSoTietKiem;
import com.SE104.quan_ly_so_tiet_kiem.entity.NguoiDung;
import com.SE104.quan_ly_so_tiet_kiem.model.TransactionType;
import com.SE104.quan_ly_so_tiet_kiem.repository.MoSoTietKiemRepository;
import com.SE104.quan_ly_so_tiet_kiem.repository.NguoiDungRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ScheduledTasksService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasksService.class);
    private static final BigDecimal INTEREST_RATE_AFTER_MATURITY = new BigDecimal("0.5");

    private final MoSoTietKiemRepository moSoTietKiemRepository;
    private final GiaoDichService giaoDichService;
    private final Clock clock;
    private final InterestService interestService;
    private final DashboardService dashboardService;
    private final NguoiDungRepository nguoiDungRepository;

    @Autowired
    public ScheduledTasksService(MoSoTietKiemRepository moSoTietKiemRepository,
                                 GiaoDichService giaoDichService,
                                 Clock clock,
                                 InterestService interestService,
                                 DashboardService dashboardService,
                                 NguoiDungRepository nguoiDungRepository) {
        this.moSoTietKiemRepository = moSoTietKiemRepository;
        this.giaoDichService = giaoDichService;
        this.clock = clock;
        this.interestService = interestService;
        this.dashboardService = dashboardService;
        this.nguoiDungRepository = nguoiDungRepository;
    }

    @Scheduled(cron = "0 5 0 * * ?")
    @Transactional
    public void dailyAccountProcessing() {
        try {
            LocalDate today = LocalDate.now(this.clock);
            logger.info("Cron job triggered at system time: {}", LocalDate.now(this.clock));
            logger.info("Running daily account processing for date: {}", today);

            // ... (Các phần 1, 2, 3, 4 xử lý lãi và đáo hạn giữ nguyên như file gốc) ...
            
            // ====================================================================
            // PHẦN THỐNG KÊ MỚI
            // ====================================================================

            // 5. Chạy thống kê toàn hệ thống
            logger.info("Starting system-wide statistics generation for {}", today);
            try {
                DashboardSummaryDTO systemSummary = dashboardService.getSystemWideSummary(today);
                logger.info("System-wide Summary for {}: Total Balance = {}, Total Active Accounts = {}, Deposits This Month = {}, Withdrawals This Month = {}",
                    today,
                    systemSummary.getTongSoDuTatCaSoCuaUser(), // Sử dụng getter tương ứng
                    systemSummary.getSoLuongSoTietKiemDangHoatDong(), // Sử dụng getter tương ứng
                    systemSummary.getTongTienDaNapThangNay(),
                    systemSummary.getTongTienDaRutThangNay());
                // Bạn có thể lưu kết quả này vào DB nếu cần
            } catch (Exception e) {
                logger.error("Error during system-wide statistics generation: {}", e.getMessage(), e);
            }

            // 6. Chạy thống kê cho từng người dùng (Tùy chọn)
            logger.info("Starting user-specific statistics generation for all users.");
            List<NguoiDung> allUsers = nguoiDungRepository.findAll();
            logger.info("Found {} users to process for statistics.", allUsers.size());
            for (NguoiDung user : allUsers) {
                try {
                    DashboardSummaryDTO userSummary = dashboardService.getMonthlySummaryForUser(user.getEmail());
                     logger.info("User Summary for '{}' (ID: {}): Total Balance = {}, Active Accounts = {}, Deposits This Month = {}, Withdrawals This Month = {}",
                        user.getTenND(),
                        user.getMaND(),
                        userSummary.getTongSoDuTatCaSoCuaUser(),
                        userSummary.getSoLuongSoTietKiemDangHoatDong(),
                        userSummary.getTongTienDaNapThangNay(),
                        userSummary.getTongTienDaRutThangNay());
                    // Bạn có thể lưu kết quả này vào DB nếu cần
                } catch (Exception e) {
                    logger.error("Error generating statistics for user ID {}: {}", user.getMaND(), e.getMessage(), e);
                }
            }
            
            logger.info("Finished daily account processing for date: {}", today);
            
        } catch (Exception e) {
            logger.error("Error in daily account processing: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    // ... (Toàn bộ các phương thức còn lại của file ScheduledTasksService.java giữ nguyên) ...
    @Transactional
    protected void processTermDepositMaturity(MoSoTietKiem account, LocalDate maturityDate) {
        logger.info("Processing term deposit maturity for Account ID {} on {}", account.getMaMoSo(), maturityDate);

        if (account.getSoDu() == null || account.getSoDu().compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Account ID {} has invalid balance: {}. Skipping interest calculation.", account.getMaMoSo(), account.getSoDu());
            account.setTrangThai(MoSoTietKiem.TrangThaiMoSo.DA_DONG);
            moSoTietKiemRepository.save(account);
            return;
        }

        BigDecimal soDuTruocLai = account.getSoDu();
        BigDecimal interest = interestService.tinhLaiTichLuyDon(account, maturityDate, account.getLaiSuatApDung(), soDuTruocLai, account.getNgayMo());

        if (interest.compareTo(BigDecimal.ZERO) > 0) {
            account.setSoDu(soDuTruocLai.add(interest));
            giaoDichService.saveTransaction(interest, TransactionType.INTEREST_ACCRUAL, account, maturityDate);
            logger.info("Term Account ID: {}. Balance before: {}. Interest: {}. New Balance: {}",
                    account.getMaMoSo(), soDuTruocLai, interest, account.getSoDu());
        } else {
            logger.warn("No interest accrued for Account ID {}. Interest calculated: {}", account.getMaMoSo(), interest);
        }

        account.setTrangThai(MoSoTietKiem.TrangThaiMoSo.DA_DAO_HAN);
        account.setLaiSuatApDung(INTEREST_RATE_AFTER_MATURITY);
        account.setNgayTraLaiCuoiCung(maturityDate);
        account.setNgayTraLaiKeTiep(maturityDate.plusMonths(1));

        try {
            logger.debug("Before saving: Account ID {}, soDu={}, status={}",
                    account.getMaMoSo(), account.getSoDu(), account.getTrangThai());
            moSoTietKiemRepository.save(account);
            logger.debug("After saving: Account ID {}, soDu={}, status={}",
                    account.getMaMoSo(), account.getSoDu(), account.getTrangThai());
        } catch (Exception e) {
            logger.error("Failed to save Account ID {}: {}", account.getMaMoSo(), e.getMessage(), e);
            throw new RuntimeException("Failed to update account on maturity: " + e.getMessage(), e);
        }
    }

    @Transactional
    protected void accrueOverdueInterest(MoSoTietKiem account, LocalDate currentDate) {
        LocalDate lastInterestDate = account.getNgayTraLaiCuoiCung() != null ? account.getNgayTraLaiCuoiCung() : account.getNgayDaoHan();
        if (lastInterestDate == null || currentDate.isBefore(lastInterestDate.plusDays(1))) {
            logger.info("Overdue Account ID {}: Not enough days since last interest/maturity date for 0.5% interest.", account.getMaMoSo());
            return;
        }

        BigDecimal currentBalance = account.getSoDu();
        BigDecimal interest = interestService.tinhLaiTichLuyDon(account, currentDate, INTEREST_RATE_AFTER_MATURITY, currentBalance, lastInterestDate);

        if (interest.compareTo(BigDecimal.ZERO) > 0) {
            account.setSoDu(currentBalance.add(interest));
            giaoDichService.saveTransaction(interest, TransactionType.INTEREST_PAYMENT, account, currentDate);
            account.setNgayTraLaiCuoiCung(currentDate);
            logger.info("Overdue Account ID: {}. Balance: {}. Interest (0.5%): {}. New Balance: {}",
                    account.getMaMoSo(), currentBalance, interest, account.getSoDu());
        }
        account.setNgayTraLaiKeTiep(currentDate.plusMonths(1));
        moSoTietKiemRepository.save(account);
    }

    @Transactional
    protected void accrueInterestForNonTermAccount(MoSoTietKiem account, LocalDate currentDate) {
        LocalDate lastInterestDate = account.getNgayTraLaiCuoiCung() != null ? account.getNgayTraLaiCuoiCung() : account.getNgayMo();
        if (currentDate.isBefore(lastInterestDate.plusDays(1))) {
            logger.info("Non-term Account ID {}: Not enough days since last interest date.", account.getMaMoSo());
            if (account.getNgayTraLaiKeTiep() == null || account.getNgayTraLaiKeTiep().isBefore(currentDate.plusMonths(1))) {
                account.setNgayTraLaiKeTiep(lastInterestDate.plusMonths(1));
                moSoTietKiemRepository.save(account);
            }
            return;
        }

        if (account.getSoTietKiemSanPham() != null &&
                account.getSoTietKiemSanPham().getSoNgayGuiToiThieuDeRut() != null &&
                account.getNgayTraLaiCuoiCung() == null &&
                ChronoUnit.DAYS.between(account.getNgayMo(), currentDate) < account.getSoTietKiemSanPham().getSoNgayGuiToiThieuDeRut()) {
            logger.info("Non-term Account ID: {}. Minimum {} deposit days not met for first interest payment. Days held: {}",
                    account.getMaMoSo(), account.getSoTietKiemSanPham().getSoNgayGuiToiThieuDeRut(), ChronoUnit.DAYS.between(account.getNgayMo(), currentDate));
            if (account.getNgayTraLaiKeTiep() == null || account.getNgayTraLaiKeTiep().isBefore(currentDate.plusMonths(1))) {
                account.setNgayTraLaiKeTiep(account.getNgayMo().plusMonths(1));
                moSoTietKiemRepository.save(account);
            }
            return;
        }

        BigDecimal currentBalance = account.getSoDu();
        BigDecimal interestRate = account.getLaiSuatApDung();
        BigDecimal interest = interestService.tinhLaiTichLuyDon(account, currentDate, interestRate, currentBalance, lastInterestDate);

        if (interest.compareTo(BigDecimal.ZERO) > 0) {
            account.setSoDu(currentBalance.add(interest));
            giaoDichService.saveTransaction(interest, TransactionType.INTEREST_PAYMENT, account, currentDate);
            account.setNgayTraLaiCuoiCung(currentDate);
            logger.info("Non-term Account ID: {}. Balance: {}. Interest: {}. New Balance: {}",
                    account.getMaMoSo(), currentBalance, interest, account.getSoDu());
        }
        account.setNgayTraLaiKeTiep(currentDate.plusMonths(1));
        moSoTietKiemRepository.save(account);
    }

    @Transactional
    protected void accrueInterestForTermAccount(MoSoTietKiem account, LocalDate currentDate) {
        LocalDate lastInterestDate = account.getNgayTraLaiCuoiCung() != null ? account.getNgayTraLaiCuoiCung() : account.getNgayMo();
        if (currentDate.isBefore(lastInterestDate.plusDays(1))) {
            logger.info("Term Account ID {}: Not enough days since last interest date.", account.getMaMoSo());
            return;
        }
        BigDecimal currentBalance = account.getSoDu();
        BigDecimal interest = interestService.tinhLaiTichLuyDon(account, currentDate, account.getLaiSuatApDung(), currentBalance, lastInterestDate);
        if (interest.compareTo(BigDecimal.ZERO) > 0) {
            account.setSoDu(currentBalance.add(interest));
            giaoDichService.saveTransaction(interest, TransactionType.INTEREST_PAYMENT, account, currentDate);
            account.setNgayTraLaiCuoiCung(currentDate);
            logger.info("Term Account ID: {}. Balance: {}. Interest: {}. New Balance: {}",
                    account.getMaMoSo(), currentBalance, interest, account.getSoDu());
        }
        account.setNgayTraLaiKeTiep(currentDate.plusMonths(1));
        moSoTietKiemRepository.save(account);
    }
}