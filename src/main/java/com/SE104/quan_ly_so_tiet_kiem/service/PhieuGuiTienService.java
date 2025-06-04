package com.SE104.quan_ly_so_tiet_kiem.service;

import com.SE104.quan_ly_so_tiet_kiem.entity.MoSoTietKiem;
import com.SE104.quan_ly_so_tiet_kiem.entity.PhieuGuiTien;
import com.SE104.quan_ly_so_tiet_kiem.entity.SoTietKiem;
import com.SE104.quan_ly_so_tiet_kiem.model.TransactionType;
import com.SE104.quan_ly_so_tiet_kiem.repository.MoSoTietKiemRepository;
import com.SE104.quan_ly_so_tiet_kiem.repository.PhieuGuiTienRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy; 
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class PhieuGuiTienService {

    @Autowired
    private PhieuGuiTienRepository phieuGuiTienRepository;
    @Autowired
    private MoSoTietKiemRepository moSoTietKiemRepository;
    @Autowired
    private GiaoDichService giaoDichService;

    private final MoSoTietKiemService moSoTietKiemService;

    @Autowired
    public PhieuGuiTienService(@Lazy MoSoTietKiemService moSoTietKiemService) {
        this.moSoTietKiemService = moSoTietKiemService;
    }


    @Transactional
    public PhieuGuiTien deposit(Integer moSoTietKiemId, BigDecimal amount, Integer userId, boolean isInitialDeposit) {
        this.moSoTietKiemService.validateUserAccess(moSoTietKiemId, userId);
        MoSoTietKiem moSoTietKiem = moSoTietKiemRepository.findById(moSoTietKiemId)
                .orElseThrow(() -> new EntityNotFoundException("Sổ tiết kiệm không tồn tại với ID: " + moSoTietKiemId));
        if (moSoTietKiem.getTrangThai() == MoSoTietKiem.TrangThaiMoSo.DA_DONG) {
            throw new IllegalStateException("Sổ tiết kiệm đã đóng, không thể gửi thêm tiền.");
        }
        SoTietKiem sanPham = moSoTietKiem.getSoTietKiemSanPham();
        if (sanPham == null) {
            throw new IllegalStateException("Thông tin sản phẩm của sổ tiết kiệm không hợp lệ.");
        }
        if (!isInitialDeposit) {
            if (amount.compareTo(new BigDecimal("100000")) < 0) {
                throw new IllegalArgumentException("Số tiền gửi thêm tối thiểu là 100.000 VND.");
            }
            if (sanPham.getKyHan() != null && sanPham.getKyHan() > 0) {
                LocalDate today = LocalDate.now();
                if (moSoTietKiem.getNgayDaoHan() == null || today.isBefore(moSoTietKiem.getNgayDaoHan())) {
                    throw new IllegalStateException("Chỉ được gửi thêm tiền vào sổ có kỳ hạn khi đến ngày đáo hạn (tái tục).");
                }
            } else {
                LocalDate ngayMoSo = moSoTietKiem.getNgayMo();
                LocalDate today = LocalDate.now();
                long daysSinceOpened = ChronoUnit.DAYS.between(ngayMoSo, today);
                if (sanPham.getSoNgayGuiToiThieuDeRut() != null && daysSinceOpened < sanPham.getSoNgayGuiToiThieuDeRut()) {
                    throw new IllegalStateException("Sổ không kỳ hạn chưa đủ điều kiện gửi thêm tiền. Cần gửi ít nhất " +
                                                    sanPham.getSoNgayGuiToiThieuDeRut() + " ngày. Đã gửi: " + daysSinceOpened + " ngày.");
                }
            }
        }
        PhieuGuiTien phieuGuiTien = new PhieuGuiTien();
        phieuGuiTien.setMoSoTietKiem(moSoTietKiem);
        phieuGuiTien.setSoTienGui(amount);
        phieuGuiTien.setNgayGui(LocalDate.now());
        PhieuGuiTien savedPhieu = phieuGuiTienRepository.save(phieuGuiTien);
        moSoTietKiem.setSoDu(moSoTietKiem.getSoDu().add(amount));
        moSoTietKiemRepository.save(moSoTietKiem);
        giaoDichService.saveTransaction(amount, TransactionType.DEPOSIT, moSoTietKiem);
        return savedPhieu;
    }
}