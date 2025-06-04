package com.SE104.quan_ly_so_tiet_kiem.service;

import com.SE104.quan_ly_so_tiet_kiem.dto.*;
import com.SE104.quan_ly_so_tiet_kiem.entity.LoaiSoTietKiem;
import com.SE104.quan_ly_so_tiet_kiem.entity.MoSoTietKiem;
import com.SE104.quan_ly_so_tiet_kiem.entity.NguoiDung;
import com.SE104.quan_ly_so_tiet_kiem.model.TransactionType;
import com.SE104.quan_ly_so_tiet_kiem.repository.*;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private NguoiDungRepository nguoiDungRepository;
    @Autowired
    private MoSoTietKiemRepository moSoTietKiemRepository;
    @Autowired
    private MoSoTietKiemService moSoTietKiemService;
    @Autowired
    private UserService userService;
    @Autowired
    private SoTietKiemService soTietKiemService;
    @Autowired
    private GiaoDichService giaoDichService;
    @Autowired
    private DangNhapRepository dangNhapRepository;
    @Autowired
    private GiaoDichRepository giaoDichRepository;
    @Autowired
    private LoaiSoTietKiemDanhMucRepository loaiSoTietKiemDanhMucRepository;


    @Transactional(readOnly = true)
    public List<UserDetailDTO> getAllUsersWithAccountDetails() {
        List<NguoiDung> users = nguoiDungRepository.findAll();
        return users.stream().map(user -> {
            UserDetailDTO dto = new UserDetailDTO();
            dto.setMaND(user.getMaND());
            dto.setTenND(user.getTenND());
            dto.setCccd(user.getCccd());
            dto.setDiaChi(user.getDiaChi());
            dto.setSdt(user.getSdt());
            if (user.getNgaySinh() != null) {
                 dto.setNgaySinh(new java.sql.Date(user.getNgaySinh().getTime()));
            } else {
                dto.setNgaySinh(null);
            }
            dto.setEmail(user.getEmail());
            dto.setVaiTro(user.getVaiTro() == 0 ? "ADMIN" : "USER");

            List<MoSoTietKiem> userAccounts = moSoTietKiemRepository.findByNguoiDung(user); 
            dto.setDanhSachSoTietKiemDaMo(
                userAccounts.stream()
                    .map(moSoTietKiemService::mapEntityToResponse)
                    .collect(Collectors.toList())
            );
            BigDecimal totalBalance = userAccounts.stream()
                .filter(acc -> acc.getTrangThai() == MoSoTietKiem.TrangThaiMoSo.DANG_HOAT_DONG)
                .map(MoSoTietKiem::getSoDu)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            dto.setTongSoDuTatCaSo(totalBalance);
            return dto;
        }).collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public UserDetailDTO getUserDetailsByUserId(Integer userId) {
        NguoiDung user = nguoiDungRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("Người dùng không tồn tại ID: " + userId));
        
        UserDetailDTO dto = new UserDetailDTO();
        dto.setMaND(user.getMaND());
        dto.setTenND(user.getTenND());
        dto.setCccd(user.getCccd());
        dto.setDiaChi(user.getDiaChi());
        dto.setSdt(user.getSdt());
         if (user.getNgaySinh() != null) {
            dto.setNgaySinh(new java.sql.Date(user.getNgaySinh().getTime()));
        } else {
            dto.setNgaySinh(null);
        }
        dto.setEmail(user.getEmail());
        dto.setVaiTro(user.getVaiTro() == 0 ? "ADMIN" : "USER");

        List<MoSoTietKiem> userAccounts = moSoTietKiemRepository.findByNguoiDung(user);
        dto.setDanhSachSoTietKiemDaMo(
            userAccounts.stream()
                .map(moSoTietKiemService::mapEntityToResponse)
                .collect(Collectors.toList())
        );
        BigDecimal totalBalance = userAccounts.stream()
            .filter(acc -> acc.getTrangThai() == MoSoTietKiem.TrangThaiMoSo.DANG_HOAT_DONG)
            .map(MoSoTietKiem::getSoDu)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTongSoDuTatCaSo(totalBalance);
        return dto;
    }

    @Transactional
    public UserResponse updateUserByAdmin(Integer userIdToUpdate, UpdateProfileDTO profileDTO, Integer adminPerformingActionId) {
        NguoiDung admin = nguoiDungRepository.findById(adminPerformingActionId)
            .orElseThrow(() -> new EntityNotFoundException("Tài khoản Admin thực hiện hành động không tồn tại."));
        if (admin.getVaiTro() != 0) {
            throw new SecurityException("Chỉ có Admin mới có quyền cập nhật thông tin người dùng.");
        }
        return userService.updateUserProfile(userIdToUpdate, profileDTO);
    }
    
    @Transactional
    public SoTietKiemDTO createSavingsProduct(SoTietKiemRequest request, Integer adminId) {
        return soTietKiemService.createSoTietKiem(request, adminId);
    }

    @Transactional
    public SoTietKiemDTO updateSavingsProduct(Integer productId, SoTietKiemRequest request, Integer adminId) {
        return soTietKiemService.updateSoTietKiem(productId, request, adminId);
    }
    
    @Transactional
    public void deleteSavingsProduct(Integer productId, Integer adminId) {
        soTietKiemService.deleteSoTietKiem(productId, adminId);
    }

    @Transactional(readOnly = true)
    public ThongKeDTO getSystemStatistics() {
        ThongKeDTO dto = new ThongKeDTO();
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        
        Date todayUtilDateStart = java.sql.Timestamp.valueOf(today.atStartOfDay());
        Date todayUtilDateEnd = java.sql.Timestamp.valueOf(today.atTime(LocalTime.MAX));
        Date startOfMonthUtilDate = java.sql.Timestamp.valueOf(startOfMonth.atStartOfDay());

        dto.setLuotTruyCapHomNay(dangNhapRepository.countByLoginTimeBetween(todayUtilDateStart, todayUtilDateEnd));
        dto.setLuotTruyCapThangNay(dangNhapRepository.countByLoginTimeBetween(startOfMonthUtilDate, todayUtilDateEnd));

        BigDecimal doanhThuHomNay = giaoDichRepository.sumSoTienByLoaiGiaoDichAndNgayThucHienBetween(TransactionType.DEPOSIT, today, today);
        dto.setDoanhThuHomNay(doanhThuHomNay != null ? doanhThuHomNay : BigDecimal.ZERO);
        
        BigDecimal doanhThuThangNay = giaoDichRepository.sumSoTienByLoaiGiaoDichAndNgayThucHienBetween(TransactionType.DEPOSIT, startOfMonth, today);
        dto.setDoanhThuThangNay(doanhThuThangNay != null ? doanhThuThangNay : BigDecimal.ZERO);
        
        dto.setTongSoNguoiDung(nguoiDungRepository.count());
        Long activeAccounts = moSoTietKiemRepository.countByTrangThai(MoSoTietKiem.TrangThaiMoSo.DANG_HOAT_DONG);
        dto.setTongSoTaiKhoanTietKiemDangHoatDong(activeAccounts != null ? activeAccounts : 0L);
        
        BigDecimal tongSoDuToanHeThong = moSoTietKiemRepository.sumSoDuByTrangThai(MoSoTietKiem.TrangThaiMoSo.DANG_HOAT_DONG);
        dto.setTongSoDuToanHeThong(tongSoDuToanHeThong != null ? tongSoDuToanHeThong : BigDecimal.ZERO);

        Pageable limitRecent = PageRequest.of(0, 10, Sort.by("ngayThucHien").descending().and(Sort.by("id").descending()));
        Page<GiaoDichDTO> recentTransactionsPage = giaoDichService.getAllSystemTransactions(limitRecent);
        dto.setGiaoDichGanDayNhat(recentTransactionsPage.getContent());

        return dto;
    }

    @Transactional(readOnly = true)
    public Page<GiaoDichDTO> getAllSystemTransactionsPaginated(Pageable pageable) {
        return giaoDichService.getAllSystemTransactions(pageable);
    }
    
    @Transactional
    public void deleteUserByAdmin(Integer userIdToDelete, Integer adminPerformingActionId) {
        NguoiDung admin = nguoiDungRepository.findById(adminPerformingActionId)
            .orElseThrow(() -> new EntityNotFoundException("Tài khoản Admin thực hiện hành động không tồn tại."));
        if (admin.getVaiTro() != 0) {
            throw new SecurityException("Chỉ có Admin mới có quyền xóa người dùng.");
        }

        NguoiDung userToDelete = nguoiDungRepository.findById(userIdToDelete)
            .orElseThrow(() -> new EntityNotFoundException("Người dùng cần xóa không tồn tại ID: " + userIdToDelete));

        if (userToDelete.getMaND().equals(adminPerformingActionId)) {
             throw new SecurityException("Admin không thể tự xóa chính mình.");
        }
        if (userToDelete.getVaiTro() == 0) {
            throw new SecurityException("Không thể xóa tài khoản Admin khác qua chức năng này.");
        }

        dangNhapRepository.deleteByNguoiDung(userToDelete);

        List<MoSoTietKiem> userAccounts = moSoTietKiemRepository.findByNguoiDung(userToDelete);
        for (MoSoTietKiem account : userAccounts) {
            // @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true) 
            // giaoDichRepository.deleteByMoSoTietKiem(account);
            // phieuGuiTienRepository.deleteByMoSoTietKiem(account);
            // phieuRutTienRepository.deleteByMoSoTietKiem(account);
            moSoTietKiemRepository.delete(account); 
        }
    }

    @Transactional(readOnly = true) 
    public List<LoaiSoTietKiemDanhMucDTO> getAllSavingCategories() {
        List<LoaiSoTietKiem> danhMucList = loaiSoTietKiemDanhMucRepository.findAll();
        return danhMucList.stream()
                .map(this::convertToLoaiSoTietKiemDanhMucDTO)
                .collect(Collectors.toList());
    }

    private LoaiSoTietKiemDanhMucDTO convertToLoaiSoTietKiemDanhMucDTO(LoaiSoTietKiem entity) {
        if (entity == null) {
            return null;
        }
        return new LoaiSoTietKiemDanhMucDTO(entity.getMaLoaiDanhMuc(), entity.getTenLoaiDanhMuc());
    }
}