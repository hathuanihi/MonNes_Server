package com.SE104.quan_ly_so_tiet_kiem.service;

import com.SE104.quan_ly_so_tiet_kiem.entity.MoSoTietKiem;
import com.SE104.quan_ly_so_tiet_kiem.entity.PhieuRutTien;
import com.SE104.quan_ly_so_tiet_kiem.entity.SoTietKiem;
import com.SE104.quan_ly_so_tiet_kiem.model.TransactionType;
import com.SE104.quan_ly_so_tiet_kiem.repository.MoSoTietKiemRepository;
import com.SE104.quan_ly_so_tiet_kiem.repository.PhieuRutTienRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; 

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class PhieuRutTienService {

    @Autowired
    private PhieuRutTienRepository phieuRutTienRepository;
    @Autowired
    private MoSoTietKiemRepository moSoTietKiemRepository;
    @Autowired
    private GiaoDichService giaoDichService;
    @Autowired
    private MoSoTietKiemService moSoTietKiemService;


    private static final BigDecimal LAI_SUAT_KHONG_KY_HAN_QD3_PERCENT = new BigDecimal("0.5"); 

    @Transactional 
    public PhieuRutTien withdraw(Integer moSoTietKiemId, BigDecimal amountFromRequest, Integer userId) {
        moSoTietKiemService.validateUserAccess(moSoTietKiemId, userId);
        MoSoTietKiem moSoTietKiem = moSoTietKiemRepository.findById(moSoTietKiemId)
                .orElseThrow(() -> new EntityNotFoundException("Sổ tiết kiệm không tồn tại với ID: " + moSoTietKiemId));
        if (moSoTietKiem.getTrangThai() == MoSoTietKiem.TrangThaiMoSo.DA_DONG) {
            throw new IllegalStateException("Sổ tiết kiệm đã đóng, không thể rút tiền.");
        }
        SoTietKiem sanPham = moSoTietKiem.getSoTietKiemSanPham();
        if (sanPham == null) {
            throw new IllegalStateException("Thông tin sản phẩm của sổ tiết kiệm không hợp lệ.");
        }
        LocalDate ngayMo = moSoTietKiem.getNgayMo();
        LocalDate homNay = LocalDate.now();
        long soNgayDaGui = ChronoUnit.DAYS.between(ngayMo, homNay);
        BigDecimal soTienRutThucTe;
        BigDecimal tienLaiThucNhan = BigDecimal.ZERO;
        BigDecimal laiSuatDaApDungKhiRut;
        if (sanPham.getKyHan() != null && sanPham.getKyHan() > 0) { 
            LocalDate ngayDaoHan = moSoTietKiem.getNgayDaoHan();
            if (ngayDaoHan == null) {
                 throw new IllegalStateException("Sổ có kỳ hạn nhưng không có ngày đáo hạn hợp lệ.");
            }
            if (homNay.isBefore(ngayDaoHan)) {
                throw new IllegalStateException("Sổ có kỳ hạn (" + sanPham.getKyHan() + " tháng) chưa đến ngày đáo hạn (" + ngayDaoHan + "). Không thể rút tiền.");
            }
            BigDecimal tongTienGocDaGui = moSoTietKiem.getSoDu();
            laiSuatDaApDungKhiRut = LAI_SUAT_KHONG_KY_HAN_QD3_PERCENT; 
            long soNgayThucTe = ChronoUnit.DAYS.between(ngayMo, homNay);
            tienLaiThucNhan = tongTienGocDaGui.multiply(laiSuatDaApDungKhiRut).multiply(BigDecimal.valueOf(soNgayThucTe)).divide(BigDecimal.valueOf(36500), 2, RoundingMode.HALF_UP);
            soTienRutThucTe = tongTienGocDaGui.add(tienLaiThucNhan);
        } else { 
            if (sanPham.getSoNgayGuiToiThieuDeRut() == null ) {
                 throw new IllegalStateException("Quy định số ngày gửi tối thiểu để rút chưa được thiết lập cho sản phẩm không kỳ hạn này.");
            }
            if (soNgayDaGui < sanPham.getSoNgayGuiToiThieuDeRut()) {
                throw new IllegalStateException("Sổ không kỳ hạn chưa đủ thời gian gửi tối thiểu (" + sanPham.getSoNgayGuiToiThieuDeRut() + " ngày). Đã gửi: " + soNgayDaGui + " ngày.");
            }
            if (amountFromRequest == null || amountFromRequest.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Số tiền rút phải lớn hơn 0.");
            }
            if (amountFromRequest.compareTo(moSoTietKiem.getSoDu()) > 0) {
                throw new IllegalArgumentException("Số tiền rút vượt quá số dư hiện có.");
            }
            soTienRutThucTe = amountFromRequest;
            laiSuatDaApDungKhiRut = sanPham.getLaiSuat();
            tienLaiThucNhan = soTienRutThucTe.multiply(laiSuatDaApDungKhiRut).multiply(BigDecimal.valueOf(soNgayDaGui)).divide(BigDecimal.valueOf(36500), 2, RoundingMode.HALF_UP);
        }
        PhieuRutTien phieuRutTien = new PhieuRutTien();
        phieuRutTien.setMoSoTietKiem(moSoTietKiem);
        phieuRutTien.setSoTienRut(soTienRutThucTe);
        phieuRutTien.setNgayRut(new Date());
        phieuRutTien.setLaiSuatKhiRut(laiSuatDaApDungKhiRut);
        phieuRutTien.setTienLaiThucNhan(tienLaiThucNhan.setScale(2, RoundingMode.HALF_UP));
        PhieuRutTien savedPhieu = phieuRutTienRepository.save(phieuRutTien);
        BigDecimal soDuConLai = moSoTietKiem.getSoDu().subtract(soTienRutThucTe);
        if (soDuConLai.compareTo(BigDecimal.ZERO) <= 0) {
            soDuConLai = BigDecimal.ZERO;
            moSoTietKiem.setTrangThai(MoSoTietKiem.TrangThaiMoSo.DA_DONG);
        }
        moSoTietKiem.setSoDu(soDuConLai.setScale(2, RoundingMode.HALF_UP));
        moSoTietKiemRepository.save(moSoTietKiem);
        giaoDichService.saveTransaction(soTienRutThucTe, TransactionType.WITHDRAW, moSoTietKiem);
        return savedPhieu;
    }

    // public List<PhieuRutTien> getAllWithdrawalsForAccount(Integer moSoTietKiemId) {
    //    MoSoTietKiem moSoTietKiem = moSoTietKiemRepository.findById(moSoTietKiemId)
    //            .orElseThrow(() -> new EntityNotFoundException("Sổ tiết kiệm không tồn tại với ID: " + moSoTietKiemId));
    //    return phieuRutTienRepository.findByMoSoTietKiem(moSoTietKiem);
    // }
}