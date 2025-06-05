package com.SE104.quan_ly_so_tiet_kiem.service;

import com.SE104.quan_ly_so_tiet_kiem.entity.MoSoTietKiem;
import com.SE104.quan_ly_so_tiet_kiem.model.TransactionType;
import com.SE104.quan_ly_so_tiet_kiem.repository.MoSoTietKiemRepository;
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
    // private static final int DAYS_IN_YEAR = 365;

    private final MoSoTietKiemRepository moSoTietKiemRepository;
    private final GiaoDichService giaoDichService;
    private final Clock clock;
    private final InterestService interestService;

    @Autowired
    public ScheduledTasksService(MoSoTietKiemRepository moSoTietKiemRepository,
                                 GiaoDichService giaoDichService,
                                 Clock clock,
                                 InterestService interestService) {
        this.moSoTietKiemRepository = moSoTietKiemRepository;
        this.giaoDichService = giaoDichService;
        this.clock = clock;
        this.interestService = interestService;
    }

    @Scheduled(cron = "0 5 0 * * ?")
    @Transactional
    public void dailyAccountProcessing() {
      try {
        LocalDate today = LocalDate.now(this.clock);
        logger.info("Cron job triggered at system time: {}", LocalDate.now(this.clock));
        logger.info("Running daily account processing for date: {}", today);

        // 1. Xử lý sổ có kỳ hạn ĐẾN HẠN HÔM NAY
        logger.debug("Querying accounts maturing on {}", today);
        List<MoSoTietKiem> accountsMaturingToday = moSoTietKiemRepository.findByTrangThaiAndNgayDaoHan(
            MoSoTietKiem.TrangThaiMoSo.DANG_HOAT_DONG, today);
        logger.info("Found {} accounts maturing today ({})", accountsMaturingToday.size(), today);
        for (MoSoTietKiem account : accountsMaturingToday) {
            logger.info("Processing term deposit maturing today: Account ID {}", account.getMaMoSo());
            processTermDepositMaturity(account, today);
        }

        // 2. Xử lý sổ có kỳ hạn ĐÃ QUÁ HẠN
        List<MoSoTietKiem> overdueAccountsForInterest = moSoTietKiemRepository
            .findByTrangThaiAndNgayTraLaiKeTiepLessThanEqual(MoSoTietKiem.TrangThaiMoSo.DA_DAO_HAN, today);
        logger.info("Found {} overdue accounts for interest processing", overdueAccountsForInterest.size());
        for (MoSoTietKiem account : overdueAccountsForInterest) {
            logger.info("Processing overdue interest (0.5%) for account ID: {}", account.getMaMoSo());
            accrueOverdueInterest(account, today);
        }

        // 3. Xử lý trả lãi định kỳ (hàng tháng) cho sổ KKH
        List<MoSoTietKiem> termAccountsForMonthlyInterest = moSoTietKiemRepository
            .findBySoTietKiemSanPham_KyHanGreaterThanAndTrangThaiAndNgayTraLaiKeTiepLessThanEqual(
                0, MoSoTietKiem.TrangThaiMoSo.DANG_HOAT_DONG, today);
        logger.info("Found {} term accounts for monthly interest processing", termAccountsForMonthlyInterest.size());
        for (MoSoTietKiem account : termAccountsForMonthlyInterest) {
            logger.info("Processing monthly interest for term account ID: {}", account.getMaMoSo());
            accrueInterestForTermAccount(account, today);
        }

        // 4. Xử lý trả lãi hàng tháng cho sổ có kỳ hạn
        // List<MoSoTietKiem> termAccountsForMonthlyInterest = moSoTietKiemRepository
        //     .findBySoTietKiemSanPham_KyHanAndTrangThaiAndNgayTraLaiKeTiepLessThanEqual(
        //         null, MoSoTietKiem.TrangThaiMoSo.DANG_HOAT_DONG, today);
        // logger.info("Found {} term accounts for monthly interest processing", termAccountsForMonthlyInterest.size());
        // for (MoSoTietKiem account : termAccountsForMonthlyInterest) {
        //     logger.info("Processing monthly interest for term account ID: {}", account.getMaMoSo());
        //     accrueInterestForTermAccount(account, today);
        // }

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