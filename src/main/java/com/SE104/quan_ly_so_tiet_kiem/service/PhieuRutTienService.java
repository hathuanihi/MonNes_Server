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
    public PhieuRutTien withdraw(Integer moSoTietKiemId, BigDecimal soTienRut, Integer userId) {
        logger.info("User ID {} attempting to withdraw {} VND from account ID: {}", userId, soTienRut, moSoTietKiemId);
        MoSoTietKiem moSoTietKiem = moSoTietKiemService.validateUserAccessAndGetAccount(moSoTietKiemId, userId);
        
        SoTietKiem sanPham = moSoTietKiem.getSoTietKiemSanPham();
        if (sanPham == null) {
            throw new IllegalStateException("Thông tin sản phẩm của sổ tiết kiệm không hợp lệ (ID Sổ: " + moSoTietKiemId + ").");
        }

        // Validate withdrawal amount
        if (soTienRut == null || soTienRut.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền rút phải lớn hơn 0.");
        }

        LocalDate ngayRutTien = LocalDate.now(this.clock);
        BigDecimal soDuHienTai = moSoTietKiem.getSoDu();
        BigDecimal tienLaiThucNhan = BigDecimal.ZERO;
        BigDecimal laiSuatTinhKhiRut = moSoTietKiem.getLaiSuatApDung(); // Mặc định

        if (sanPham.getKyHan() != null && sanPham.getKyHan() > 0) { // Sổ có kỳ hạn
            LocalDate ngayDaoHan = moSoTietKiem.getNgayDaoHan();
            if (ngayDaoHan == null) {
                throw new IllegalStateException("Sổ có kỳ hạn (ID: " + moSoTietKiemId + ") nhưng không có ngày đáo hạn hợp lệ.");
            }

            // Chỉ được rút từ ngày đáo hạn trở đi
            if (ngayRutTien.isBefore(ngayDaoHan)) {
                throw new IllegalStateException("Sổ tiết kiệm có kỳ hạn (ID: " + moSoTietKiemId + ") chỉ được rút từ ngày đáo hạn (" + ngayDaoHan.format(java.time.format.DateTimeFormatter.ISO_DATE) + ") trở đi. Không thể rút tiền.");
            }
            
            // Phải rút hết toàn bộ số dư
            if (soTienRut.compareTo(soDuHienTai) != 0) {
                throw new IllegalStateException("Sổ tiết kiệm có kỳ hạn phải rút hết toàn bộ số dư (" + soDuHienTai + " VND). Không thể rút một phần.");
            }
            
            if (ngayRutTien.isEqual(ngayDaoHan)) {
                // Rút trong ngày đáo hạn - tính lãi với lãi suất tương ứng (lãi suất kỳ hạn)
                LocalDate ngayBatDauTinhLai = moSoTietKiem.getNgayTraLaiCuoiCung() != null ? moSoTietKiem.getNgayTraLaiCuoiCung() : moSoTietKiem.getNgayMo();
                tienLaiThucNhan = interestService.tinhLaiDonTheoThoiGian(soDuHienTai, moSoTietKiem.getLaiSuatApDung(), ngayBatDauTinhLai, ngayRutTien);
                laiSuatTinhKhiRut = moSoTietKiem.getLaiSuatApDung();
                logger.info("Term Account ID {}: Withdrawing on maturity date with term interest rate ({}%). Interest: {}", moSoTietKiemId, moSoTietKiem.getLaiSuatApDung(), tienLaiThucNhan);
            } else {
                // Rút sau ngày đáo hạn - tính lãi với lãi suất không kỳ hạn (0.5%)
                LocalDate ngayBatDauTinhLai = moSoTietKiem.getNgayTraLaiCuoiCung() != null ? moSoTietKiem.getNgayTraLaiCuoiCung() : ngayDaoHan;
                tienLaiThucNhan = interestService.tinhLaiDonTheoThoiGian(soDuHienTai, LAI_SUAT_SAU_DAO_HAN_DEFAULT, ngayBatDauTinhLai, ngayRutTien);
                laiSuatTinhKhiRut = LAI_SUAT_SAU_DAO_HAN_DEFAULT;
                logger.info("Term Account ID {}: Withdrawing after maturity date with non-term interest rate (0.5%). Interest: {}", moSoTietKiemId, tienLaiThucNhan);
            }
    

        } else { // Sổ không kỳ hạn
            if (sanPham.getSoNgayGuiToiThieuDeRut() == null ) {
                throw new IllegalStateException("Quy định số ngày gửi tối thiểu để rút chưa được thiết lập cho sản phẩm không kỳ hạn này.");
            }
            long soNgayDaGui = ChronoUnit.DAYS.between(moSoTietKiem.getNgayMo(), ngayRutTien);
            if (soNgayDaGui < sanPham.getSoNgayGuiToiThieuDeRut()) {
                throw new IllegalStateException("Sổ không kỳ hạn chưa đủ thời gian gửi tối thiểu (" + sanPham.getSoNgayGuiToiThieuDeRut() + " ngày). Đã gửi: " + soNgayDaGui + " ngày.");
            }

            // Kiểm tra số dư có đủ để rút không
            if (soTienRut.compareTo(soDuHienTai) > 0) {
                throw new IllegalStateException("Số tiền rút (" + soTienRut + " VND) vượt quá số dư hiện có (" + soDuHienTai + " VND).");
            }

            // Tính lãi cho sổ KKH từ lần trả lãi cuối cùng (hoặc ngày mở) đến ngày rút
            LocalDate ngayBatDauTinhLaiKKH = moSoTietKiem.getNgayTraLaiCuoiCung() != null ? moSoTietKiem.getNgayTraLaiCuoiCung() : moSoTietKiem.getNgayMo();
            tienLaiThucNhan = interestService.tinhLaiDonTheoThoiGian(soDuHienTai, moSoTietKiem.getLaiSuatApDung(), ngayBatDauTinhLaiKKH, ngayRutTien);
            laiSuatTinhKhiRut = moSoTietKiem.getLaiSuatApDung();
            logger.info("Non-term Account ID {}: Calculating interest on withdrawal. Interest: {}", moSoTietKiemId, tienLaiThucNhan);
        }

        // Tạo phiếu rút tiền
        PhieuRutTien phieuRutTien = new PhieuRutTien();
        phieuRutTien.setMoSoTietKiem(moSoTietKiem);
        phieuRutTien.setSoTienRut(soTienRut); 
        phieuRutTien.setNgayRut(Date.from(ngayRutTien.atStartOfDay(clock.getZone()).toInstant())); 
        phieuRutTien.setLaiSuatKhiRut(laiSuatTinhKhiRut);
        phieuRutTien.setTienLaiThucNhan(tienLaiThucNhan.setScale(2, RoundingMode.HALF_UP));
        PhieuRutTien savedPhieu = phieuRutTienRepository.save(phieuRutTien);

        // Cập nhật số dư sổ tiết kiệm
        BigDecimal soDuMoi = soDuHienTai.subtract(soTienRut);
        moSoTietKiem.setSoDu(soDuMoi);
        
        // Nếu rút hết tiền (số dư = 0) thì đóng sổ
        if (soDuMoi.compareTo(BigDecimal.ZERO) == 0) {
            moSoTietKiem.setTrangThai(MoSoTietKiem.TrangThaiMoSo.DA_DONG);
            logger.info("Account ID {} closed due to zero balance after withdrawal", moSoTietKiemId);
        }
        
        moSoTietKiemRepository.save(moSoTietKiem);

        giaoDichService.saveTransaction(soTienRut, TransactionType.WITHDRAW, moSoTietKiem, ngayRutTien);

        logger.info("User ID {} successfully withdrew {} VND (Interest: {}) from account ID {}. New balance: {}", 
                    userId, soTienRut, tienLaiThucNhan, moSoTietKiemId, soDuMoi);
        return savedPhieu;
    }
}