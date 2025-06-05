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
    private final InterestService interestService; // Inject InterestService
    private final Clock clock;

    private static final BigDecimal LAI_SUAT_SAU_DAO_HAN_DEFAULT = new BigDecimal("0.5"); 

    @Autowired
    public PhieuRutTienService(Clock clock, 
                               PhieuRutTienRepository phieuRutTienRepository,
                               MoSoTietKiemRepository moSoTietKiemRepository,
                               GiaoDichService giaoDichService,
                               MoSoTietKiemService moSoTietKiemService,
                               InterestService interestService) { // Inject InterestService
        this.phieuRutTienRepository = phieuRutTienRepository;
        this.moSoTietKiemRepository = moSoTietKiemRepository;
        this.giaoDichService = giaoDichService;
        this.moSoTietKiemService = moSoTietKiemService;
        this.interestService = interestService;
        this.clock = clock;
    }

    @Transactional 
    public PhieuRutTien withdraw(Integer moSoTietKiemId, BigDecimal amountFromRequestIgnored, Integer userId) { // amountFromRequest không còn dùng vì rút toàn bộ
        logger.info("User ID {} attempting to withdraw from account ID: {}", userId, moSoTietKiemId);
        MoSoTietKiem moSoTietKiem = moSoTietKiemService.validateUserAccessAndGetAccount(moSoTietKiemId, userId);
        
        SoTietKiem sanPham = moSoTietKiem.getSoTietKiemSanPham();
        if (sanPham == null) {
            throw new IllegalStateException("Thông tin sản phẩm của sổ tiết kiệm không hợp lệ (ID Sổ: " + moSoTietKiemId + ").");
        }

        LocalDate ngayRutTien = LocalDate.now(this.clock);
        BigDecimal soDuHienTai = moSoTietKiem.getSoDu();
        BigDecimal tienLaiThucNhan = BigDecimal.ZERO;
        BigDecimal laiSuatTinhKhiRut = moSoTietKiem.getLaiSuatApDung(); // Mặc định
        boolean daTinhLaiKyHan = false; // Cờ để biết lãi kỳ hạn đã được ScheduledTask xử lý chưa

        if (sanPham.getKyHan() != null && sanPham.getKyHan() > 0) { // Sổ có kỳ hạn
            LocalDate ngayDaoHan = moSoTietKiem.getNgayDaoHan();
            if (ngayDaoHan == null) {
                throw new IllegalStateException("Sổ có kỳ hạn (ID: " + moSoTietKiemId + ") nhưng không có ngày đáo hạn hợp lệ.");
            }

            if (ngayRutTien.isBefore(ngayDaoHan)) {
                throw new IllegalStateException("Sổ tiết kiệm (ID: " + moSoTietKiemId + ") chưa đến ngày đáo hạn (" + ngayDaoHan.format(java.time.format.DateTimeFormatter.ISO_DATE) + "). Không thể rút tiền.");
            }
            
            if (ngayRutTien.isEqual(ngayDaoHan) && moSoTietKiem.getTrangThai() == MoSoTietKiem.TrangThaiMoSo.DANG_HOAT_DONG) {
                 // Tính lãi cho kỳ hạn vừa kết thúc (giống logic trong ScheduledTask)
                tienLaiThucNhan = interestService.tinhLaiDonTheoThoiGian(soDuHienTai, moSoTietKiem.getLaiSuatApDung(), moSoTietKiem.getNgayMo(), ngayDaoHan);
                laiSuatTinhKhiRut = moSoTietKiem.getLaiSuatApDung();
                daTinhLaiKyHan = true; // Đã tính lãi của kỳ hạn này
                logger.info("Account ID {}: Calculating maturity interest on withdrawal. Interest: {}", moSoTietKiemId, tienLaiThucNhan);
            }
            // Nếu rút SAU ngày đáo hạn (sổ đang ở trạng thái DA_DAO_HAN_CHO_XU_LY và hưởng lãi 0.5%)
            else if (ngayRutTien.isAfter(ngayDaoHan) && moSoTietKiem.getTrangThai() == MoSoTietKiem.TrangThaiMoSo.DA_DAO_HAN) {
                LocalDate ngayBatDauTinhLaiSauDaoHan = moSoTietKiem.getNgayTraLaiCuoiCung() != null ? moSoTietKiem.getNgayTraLaiCuoiCung() : ngayDaoHan;
                tienLaiThucNhan = interestService.tinhLaiDonTheoThoiGian(soDuHienTai, LAI_SUAT_SAU_DAO_HAN_DEFAULT, ngayBatDauTinhLaiSauDaoHan, ngayRutTien);
                laiSuatTinhKhiRut = LAI_SUAT_SAU_DAO_HAN_DEFAULT;
                logger.info("Account ID {}: Calculating post-maturity interest (0.5%) on withdrawal. Interest: {}", moSoTietKiemId, tienLaiThucNhan);
            }
    

        } else { // Sổ không kỳ hạn
            if (sanPham.getSoNgayGuiToiThieuDeRut() == null ) {
                throw new IllegalStateException("Quy định số ngày gửi tối thiểu để rút chưa được thiết lập cho sản phẩm không kỳ hạn này.");
            }
            long soNgayDaGui = ChronoUnit.DAYS.between(moSoTietKiem.getNgayMo(), ngayRutTien);
            if (soNgayDaGui < sanPham.getSoNgayGuiToiThieuDeRut()) {
                throw new IllegalStateException("Sổ không kỳ hạn chưa đủ thời gian gửi tối thiểu (" + sanPham.getSoNgayGuiToiThieuDeRut() + " ngày). Đã gửi: " + soNgayDaGui + " ngày.");
            }
            // Tính lãi cho sổ KKH từ lần trả lãi cuối cùng (hoặc ngày mở) đến ngày rút
            LocalDate ngayBatDauTinhLaiKKH = moSoTietKiem.getNgayTraLaiCuoiCung() != null ? moSoTietKiem.getNgayTraLaiCuoiCung() : moSoTietKiem.getNgayMo();
            tienLaiThucNhan = interestService.tinhLaiDonTheoThoiGian(soDuHienTai, moSoTietKiem.getLaiSuatApDung(), ngayBatDauTinhLaiKKH, ngayRutTien);
            laiSuatTinhKhiRut = moSoTietKiem.getLaiSuatApDung();
            logger.info("Non-term Account ID {}: Calculating interest on withdrawal. Interest: {}", moSoTietKiemId, tienLaiThucNhan);
        }

        BigDecimal soTienRutToanBo = soDuHienTai.add(tienLaiThucNhan);

        if (soTienRutToanBo.compareTo(BigDecimal.ZERO) < 0) { // Không thể rút số tiền âm
             logger.error("Calculated withdrawal amount is negative for account ID {}. Amount: {}", moSoTietKiemId, soTienRutToanBo);
            throw new IllegalStateException("Lỗi tính toán: Số tiền rút không thể âm.");
        }
        
        // Tạo phiếu rút tiền
        PhieuRutTien phieuRutTien = new PhieuRutTien();
        phieuRutTien.setMoSoTietKiem(moSoTietKiem);
        phieuRutTien.setSoTienRut(soTienRutToanBo); 
        phieuRutTien.setNgayRut(Date.from(ngayRutTien.atStartOfDay(clock.getZone()).toInstant())); 
        phieuRutTien.setLaiSuatKhiRut(laiSuatTinhKhiRut);
        phieuRutTien.setTienLaiThucNhan(tienLaiThucNhan.setScale(2, RoundingMode.HALF_UP));
        PhieuRutTien savedPhieu = phieuRutTienRepository.save(phieuRutTien);

        // Cập nhật sổ tiết kiệm: số dư về 0 và đóng sổ
        moSoTietKiem.setSoDu(BigDecimal.ZERO);
        moSoTietKiem.setTrangThai(MoSoTietKiem.TrangThaiMoSo.DA_DONG);
        moSoTietKiemRepository.save(moSoTietKiem);

        giaoDichService.saveTransaction(soTienRutToanBo, TransactionType.WITHDRAW, moSoTietKiem, ngayRutTien);

        logger.info("User ID {} successfully withdrew {} VND (Interest: {}) from account ID {}. Account closed.", 
                    userId, soTienRutToanBo, tienLaiThucNhan, moSoTietKiemId);
        return savedPhieu;
    }
}