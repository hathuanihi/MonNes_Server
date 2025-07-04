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

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void dailyAccountProcessing() {
        try {
            LocalDate today = LocalDate.now(this.clock);
            logger.info("Cron job triggered at system time: {}", LocalDate.now(this.clock));
            logger.info("Running daily account processing for date: {}", today);

            logger.info("Starting term deposit maturity processing for {}", today);
            List<MoSoTietKiem> termAccountsMaturing = moSoTietKiemRepository.findByNgayDaoHanAndTrangThai(
                today, MoSoTietKiem.TrangThaiMoSo.DANG_HOAT_DONG);
            logger.info("Found {} term accounts maturing on {}", termAccountsMaturing.size(), today);
            
            for (MoSoTietKiem account : termAccountsMaturing) {
                try {
                    processTermDepositMaturity(account, today);
                } catch (Exception e) {
                    logger.error("Error processing maturity for account ID {}: {}", account.getMaMoSo(), e.getMessage(), e);
                }
            }

            logger.info("Starting overdue interest processing for {}", today);
            List<MoSoTietKiem> overdueAccounts = moSoTietKiemRepository.findByTrangThaiAndNgayTraLaiKeTiepLessThanEqual(
                MoSoTietKiem.TrangThaiMoSo.DA_DAO_HAN, today);
            logger.info("Found {} overdue accounts for interest calculation on {}", overdueAccounts.size(), today);
            
            for (MoSoTietKiem account : overdueAccounts) {
                try {
                    accrueOverdueInterest(account, today);
                } catch (Exception e) {
                    logger.error("Error processing overdue interest for account ID {}: {}", account.getMaMoSo(), e.getMessage(), e);
                }
            }

            logger.info("Starting non-term account interest processing for {}", today);
            List<MoSoTietKiem> nonTermAccounts = moSoTietKiemRepository.findByTrangThaiAndNgayTraLaiKeTiepLessThanEqualAndSoTietKiemSanPham_KyHanIsNull(
                MoSoTietKiem.TrangThaiMoSo.DANG_HOAT_DONG, today);
            logger.info("Found {} non-term accounts for interest calculation on {}", nonTermAccounts.size(), today);
            
            for (MoSoTietKiem account : nonTermAccounts) {
                try {
                    accrueInterestForNonTermAccount(account, today);
                } catch (Exception e) {
                    logger.error("Error processing interest for non-term account ID {}: {}", account.getMaMoSo(), e.getMessage(), e);
                }
            }

            logger.info("Starting term account interest processing for {}", today);
            List<MoSoTietKiem> activeTermAccounts = moSoTietKiemRepository.findByTrangThaiAndNgayTraLaiKeTiepLessThanEqualAndSoTietKiemSanPham_KyHanIsNotNull(
                MoSoTietKiem.TrangThaiMoSo.DANG_HOAT_DONG, today);
            logger.info("Found {} active term accounts for interest calculation on {}", activeTermAccounts.size(), today);
            
            for (MoSoTietKiem account : activeTermAccounts) {
                try {
                    // Chỉ tính lãi nếu chưa đến ngày đáo hạn
                    if (account.getNgayDaoHan() != null && today.isBefore(account.getNgayDaoHan())) {
                        accrueInterestForTermAccount(account, today);
                    }
                } catch (Exception e) {
                    logger.error("Error processing interest for term account ID {}: {}", account.getMaMoSo(), e.getMessage(), e);
                }
            }
            
            // ====================================================================
            // PHẦN THỐNG KÊ MỚI
            // ====================================================================

            logger.info("Starting system-wide statistics generation for {}", today);
            try {
                DashboardSummaryDTO systemSummary = dashboardService.getSystemWideSummary(today);
                logger.info("System-wide Summary for {}: Total Balance = {}, Total Active Accounts = {}, Deposits This Month = {}, Withdrawals This Month = {}",
                    today,
                    systemSummary.getTongSoDuTatCaSoCuaUser(), 
                    systemSummary.getSoLuongSoTietKiemDangHoatDong(), 
                    systemSummary.getTongTienDaNapThangNay(),
                    systemSummary.getTongTienDaRutThangNay());
            } catch (Exception e) {
                logger.error("Error during system-wide statistics generation: {}", e.getMessage(), e);
            }

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
            giaoDichService.saveTransaction(interest, TransactionType.INTEREST, account, maturityDate);
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
        BigDecimal interest = interestService.tinhLaiTichLuyDon(account, currentDate, INTEREST_RATE_AFTER_MATURITY, currentBalance, lastInterestDate);        if (interest.compareTo(BigDecimal.ZERO) > 0) {
            account.setSoDu(currentBalance.add(interest));
            giaoDichService.saveTransaction(interest, TransactionType.INTEREST, account, currentDate);
            account.setNgayTraLaiCuoiCung(currentDate);
            logger.info("Overdue Account ID: {}. Balance: {}. Interest (0.5%): {}. New Balance: {}",
                    account.getMaMoSo(), currentBalance, interest, account.getSoDu());
        }
        account.setNgayTraLaiKeTiep(currentDate.plusMonths(1));
        moSoTietKiemRepository.save(account);
    }    
    @Transactional
    protected void accrueInterestForNonTermAccount(MoSoTietKiem account, LocalDate currentDate) {
        // Kiểm tra số ngày gửi tối thiểu (15 ngày) cho sổ không kỳ hạn
        long daysDeposited = ChronoUnit.DAYS.between(account.getNgayMo(), currentDate);
        if (account.getSoTietKiemSanPham() != null &&
                account.getSoTietKiemSanPham().getSoNgayGuiToiThieuDeRut() != null &&
                daysDeposited < account.getSoTietKiemSanPham().getSoNgayGuiToiThieuDeRut()) {
            logger.info("Non-term Account ID {}: Minimum {} deposit days not met for interest calculation. Days held: {}",
                    account.getMaMoSo(), account.getSoTietKiemSanPham().getSoNgayGuiToiThieuDeRut(), daysDeposited);
            // Cập nhật ngày trả lãi kế tiếp
            if (account.getNgayTraLaiKeTiep() == null || account.getNgayTraLaiKeTiep().isBefore(currentDate.plusMonths(1))) {
                account.setNgayTraLaiKeTiep(account.getNgayMo().plusDays(account.getSoTietKiemSanPham().getSoNgayGuiToiThieuDeRut()).plusDays(1));
                moSoTietKiemRepository.save(account);
            }
            return;
        }

        LocalDate lastInterestDate = account.getNgayTraLaiCuoiCung() != null ? account.getNgayTraLaiCuoiCung() : account.getNgayMo();
        if (currentDate.isBefore(lastInterestDate.plusDays(1))) {
            logger.info("Non-term Account ID {}: Not enough days since last interest date.", account.getMaMoSo());
            if (account.getNgayTraLaiKeTiep() == null || account.getNgayTraLaiKeTiep().isBefore(currentDate.plusMonths(1))) {
                account.setNgayTraLaiKeTiep(lastInterestDate.plusMonths(1));
                moSoTietKiemRepository.save(account);
            }
            return;
        }

        BigDecimal currentBalance = account.getSoDu();
        if (currentBalance == null || currentBalance.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Non-term Account ID {} has invalid balance: {}. Skipping interest calculation.", account.getMaMoSo(), currentBalance);
            account.setNgayTraLaiKeTiep(currentDate.plusMonths(1));
            moSoTietKiemRepository.save(account);
            return;
        }

        BigDecimal interestRate = account.getLaiSuatApDung();
        BigDecimal interest = interestService.tinhLaiTichLuyDon(account, currentDate, interestRate, currentBalance, lastInterestDate);
        
        if (interest.compareTo(BigDecimal.ZERO) > 0) {
            account.setSoDu(currentBalance.add(interest));
            giaoDichService.saveTransaction(interest, TransactionType.INTEREST, account, currentDate);
            account.setNgayTraLaiCuoiCung(currentDate);
            logger.info("Non-term Account ID: {}. Balance: {}. Interest ({}%): {}. New Balance: {}",
                    account.getMaMoSo(), currentBalance, interestRate, interest, account.getSoDu());
        } else {
            logger.info("Non-term Account ID {}: No interest accrued. Interest calculated: {}", account.getMaMoSo(), interest);
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
        BigDecimal interest = interestService.tinhLaiTichLuyDon(account, currentDate, account.getLaiSuatApDung(), currentBalance, lastInterestDate);        if (interest.compareTo(BigDecimal.ZERO) > 0) {
            account.setSoDu(currentBalance.add(interest));
            giaoDichService.saveTransaction(interest, TransactionType.INTEREST, account, currentDate);
            account.setNgayTraLaiCuoiCung(currentDate);
            logger.info("Term Account ID: {}. Balance: {}. Interest: {}. New Balance: {}",
                    account.getMaMoSo(), currentBalance, interest, account.getSoDu());
        }
        account.setNgayTraLaiKeTiep(currentDate.plusMonths(1));
        moSoTietKiemRepository.save(account);
    }
}