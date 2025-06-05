package com.SE104.quan_ly_so_tiet_kiem.service;

import com.SE104.quan_ly_so_tiet_kiem.dto.MoSoTietKiemRequest;
import com.SE104.quan_ly_so_tiet_kiem.dto.MoSoTietKiemResponse;
import com.SE104.quan_ly_so_tiet_kiem.entity.MoSoTietKiem;
import com.SE104.quan_ly_so_tiet_kiem.entity.NguoiDung;
import com.SE104.quan_ly_so_tiet_kiem.entity.SoTietKiem;
import com.SE104.quan_ly_so_tiet_kiem.repository.MoSoTietKiemRepository;
import com.SE104.quan_ly_so_tiet_kiem.repository.NguoiDungRepository;
import com.SE104.quan_ly_so_tiet_kiem.repository.SoTietKiemRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Clock;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MoSoTietKiemService {

    // @Autowired
    // private MoSoTietKiemRepository moSoTietKiemRepository;
    // @Autowired
    // private SoTietKiemRepository soTietKiemRepository;
    // @Autowired
    // private NguoiDungRepository nguoiDungRepository;
    // @Autowired
    // private PhieuGuiTienService phieuGuiTienService;
    private final MoSoTietKiemRepository moSoTietKiemRepository;
    private final SoTietKiemRepository soTietKiemRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final PhieuGuiTienService phieuGuiTienService;
    private final Clock clock;

    @Autowired
    public MoSoTietKiemService(Clock clock, 
                               MoSoTietKiemRepository moSoTietKiemRepository,
                               SoTietKiemRepository soTietKiemRepository,
                               NguoiDungRepository nguoiDungRepository,
                               PhieuGuiTienService phieuGuiTienService) {
        this.moSoTietKiemRepository = moSoTietKiemRepository;
        this.soTietKiemRepository = soTietKiemRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.phieuGuiTienService = phieuGuiTienService;
        this.clock = clock;
    }

    @Transactional(Transactional.TxType.SUPPORTS) // readOnly = true cho Spring
    public List<MoSoTietKiemResponse> getUserSavingsAccounts(Integer userId) {
        // NguoiDung nguoiDung = nguoiDungRepository.findById(userId) 
        //     .orElseThrow(() -> new EntityNotFoundException("Người dùng không tồn tại với ID: " + userId));
        return moSoTietKiemRepository.findByNguoiDung_MaND(userId)
                .stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public MoSoTietKiemResponse getSavingsAccountDetails(Integer moSoTietKiemId, Integer userId) {
        MoSoTietKiem moSoTietKiem = moSoTietKiemRepository.findById(moSoTietKiemId)
                .orElseThrow(() -> new EntityNotFoundException("Sổ tiết kiệm không tồn tại với ID: " + moSoTietKiemId));
        if (!moSoTietKiem.getNguoiDung().getMaND().equals(userId)) {
            throw new SecurityException("Không có quyền truy cập sổ tiết kiệm này.");
        }
        return mapEntityToResponse(moSoTietKiem);
    }

    @Transactional(Transactional.TxType.REQUIRED) 
    public MoSoTietKiemResponse createSavingsAccount(MoSoTietKiemRequest request, Integer userId) {
        NguoiDung nguoiDung = nguoiDungRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Người dùng không tồn tại với ID: " + userId));

        if (nguoiDung.getTenND() == null || nguoiDung.getTenND().trim().isEmpty() ||
            nguoiDung.getCccd() == null || nguoiDung.getCccd().trim().isEmpty() ||
            nguoiDung.getDiaChi() == null || nguoiDung.getDiaChi().trim().isEmpty()) {
            throw new IllegalStateException("Vui lòng cập nhật đầy đủ thông tin cá nhân (Họ tên, CCCD, Địa chỉ) trong hồ sơ trước khi mở sổ tiết kiệm.");
        }

        SoTietKiem sanPhamSoTietKiem = soTietKiemRepository.findById(request.getSoTietKiemSanPhamId())
                .orElseThrow(() -> new EntityNotFoundException("Sản phẩm sổ tiết kiệm không tồn tại với ID: " + request.getSoTietKiemSanPhamId()));

        if (request.getSoTienGuiBanDau().compareTo(BigDecimal.valueOf(sanPhamSoTietKiem.getTienGuiBanDauToiThieu())) < 0) {
            throw new IllegalArgumentException("Số tiền gửi ban đầu ("+ request.getSoTienGuiBanDau() +") phải lớn hơn hoặc bằng " + sanPhamSoTietKiem.getTienGuiBanDauToiThieu() + " VND cho sản phẩm này.");
        }

        MoSoTietKiem moSo = new MoSoTietKiem();
        moSo.setTenSoMo(request.getTenSoMo());
        moSo.setSoTietKiemSanPham(sanPhamSoTietKiem);
        moSo.setNguoiDung(nguoiDung);
        moSo.setNgayMo(LocalDate.now(this.clock));
        moSo.setSoDu(BigDecimal.ZERO); 
        moSo.setTrangThai(MoSoTietKiem.TrangThaiMoSo.DANG_HOAT_DONG);
        moSo.setLaiSuatApDung(sanPhamSoTietKiem.getLaiSuat());
        moSo.tinhNgayDaoHanVaLaiSuatApDung(); 

        MoSoTietKiem savedMoSo = moSoTietKiemRepository.save(moSo);

        // isInitialDeposit = true
        phieuGuiTienService.deposit(savedMoSo.getMaMoSo(), request.getSoTienGuiBanDau(), nguoiDung.getMaND(), true);
        
        MoSoTietKiem updatedMoSo = moSoTietKiemRepository.findById(savedMoSo.getMaMoSo())
            .orElseThrow(() -> new EntityNotFoundException("Lỗi khi tạo sổ: không tìm thấy sổ vừa tạo. ID: " + savedMoSo.getMaMoSo()));


        return mapEntityToResponse(updatedMoSo);
    }

    public void validateUserAccess(Integer moSoTietKiemId, Integer userId) {
        MoSoTietKiem moSo = moSoTietKiemRepository.findById(moSoTietKiemId)
                .orElseThrow(() -> new EntityNotFoundException("Sổ tiết kiệm không tồn tại với ID: " + moSoTietKiemId));
        if (moSo.getNguoiDung() == null || !moSo.getNguoiDung().getMaND().equals(userId)) {
            throw new SecurityException("Không có quyền truy cập sổ tiết kiệm này hoặc thông tin người dùng của sổ không hợp lệ.");
        }
    }

    public MoSoTietKiemResponse mapEntityToResponse(MoSoTietKiem moSo) {
        if (moSo == null) return null;
        MoSoTietKiemResponse response = new MoSoTietKiemResponse();
        response.setMaMoSo(moSo.getMaMoSo());
        response.setTenSoMo(moSo.getTenSoMo());
        response.setNgayMo(moSo.getNgayMo());
        response.setNgayDaoHan(moSo.getNgayDaoHan());
        response.setSoDuHienTai(moSo.getSoDu());
        response.setTrangThaiMoSo(moSo.getTrangThai().name());

        if (moSo.getNguoiDung() != null) {
            response.setTenNguoiDung(moSo.getNguoiDung().getTenND());
        }

        if (moSo.getSoTietKiemSanPham() != null) {
            SoTietKiem sanPham = moSo.getSoTietKiemSanPham();
            response.setMaSanPhamSoTietKiem(sanPham.getMaSo());
            response.setTenSanPhamSoTietKiem(sanPham.getTenSo());
            response.setKyHanSanPham(sanPham.getKyHan());
            response.setLaiSuatSanPhamHienTai(sanPham.getLaiSuat());
        }
        response.setLaiSuatApDungChoSoNay(moSo.getLaiSuatApDung());

        return response;
    }

    @Transactional
    public void updateNgayMo(Integer moSoTietKiemId, LocalDate newNgayMo) {
        MoSoTietKiem moSo = moSoTietKiemRepository.findById(moSoTietKiemId)
            .orElseThrow(() -> new EntityNotFoundException("Sổ tiết kiệm không tồn tại với ID: " + moSoTietKiemId));
        moSo.setNgayMo(newNgayMo);
        // Cập nhật ngày đáo hạn nếu là sổ có kỳ hạn
        if (moSo.getSoTietKiemSanPham() != null && moSo.getSoTietKiemSanPham().getKyHan() != null && moSo.getSoTietKiemSanPham().getKyHan() > 0) {
            int kyHanThang = moSo.getSoTietKiemSanPham().getKyHan();
            moSo.setNgayDaoHan(newNgayMo.plusMonths(kyHanThang));
        }
        moSoTietKiemRepository.save(moSo);
    }

    @Transactional
    public void autoCloseIfNegativeOrZeroBalance(Integer moSoTietKiemId) {
        MoSoTietKiem moSo = moSoTietKiemRepository.findById(moSoTietKiemId)
            .orElseThrow(() -> new EntityNotFoundException("Sổ tiết kiệm không tồn tại với ID: " + moSoTietKiemId));
        if (moSo.getSoDu() == null || moSo.getSoDu().compareTo(BigDecimal.ZERO) <= 0) {
            moSo.setSoDu(BigDecimal.ZERO);
            moSo.setTrangThai(MoSoTietKiem.TrangThaiMoSo.DA_DONG);
            moSoTietKiemRepository.save(moSo);
        } else if (moSo.getSoDu().compareTo(BigDecimal.ZERO) < 0) {
            moSo.setSoDu(BigDecimal.ZERO);
            moSoTietKiemRepository.save(moSo);
        }
    }
}