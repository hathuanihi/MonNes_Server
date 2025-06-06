package com.SE104.quan_ly_so_tiet_kiem.service;

import com.SE104.quan_ly_so_tiet_kiem.dto.*;
import com.SE104.quan_ly_so_tiet_kiem.entity.LoaiSoTietKiem;
import com.SE104.quan_ly_so_tiet_kiem.entity.MoSoTietKiem;
import com.SE104.quan_ly_so_tiet_kiem.entity.NguoiDung;
import com.SE104.quan_ly_so_tiet_kiem.model.TransactionType;
import com.SE104.quan_ly_so_tiet_kiem.repository.*;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.autoconfigure.kafka.KafkaProperties.Admin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Clock;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final Clock clock;

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
    @Autowired
    public AdminService(Clock clock,
                        NguoiDungRepository nguoiDungRepository,
                        MoSoTietKiemRepository moSoTietKiemRepository,
                        MoSoTietKiemService moSoTietKiemService,
                        UserService userService,
                        SoTietKiemService soTietKiemService,
                        GiaoDichService giaoDichService,
                        DangNhapRepository dangNhapRepository,
                        GiaoDichRepository giaoDichRepository, 
                        LoaiSoTietKiemDanhMucRepository loaiSoTietKiemDanhMucRepository) {
        this.clock = clock;
        this.nguoiDungRepository = nguoiDungRepository;
        this.moSoTietKiemRepository = moSoTietKiemRepository;
        this.moSoTietKiemService = moSoTietKiemService;
        this.userService = userService;
        this.soTietKiemService = soTietKiemService;
        this.giaoDichService = giaoDichService;
        this.dangNhapRepository = dangNhapRepository;
        this.giaoDichRepository = giaoDichRepository; // Gán GiaoDichRepository
        this.loaiSoTietKiemDanhMucRepository = loaiSoTietKiemDanhMucRepository;
    }


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
        try {
            return userService.updateUserProfile(userIdToUpdate, profileDTO);
        } catch (EntityNotFoundException ex) {
            throw new EntityNotFoundException("Không tìm thấy người dùng với ID: " + userIdToUpdate);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("Email hoặc số điện thoại đã tồn tại trong hệ thống.");
        } catch (jakarta.validation.ConstraintViolationException ex) {
            throw new IllegalArgumentException("Dữ liệu không hợp lệ: " + ex.getMessage());
        } catch (Exception ex) {
            throw new RuntimeException("Lỗi khi cập nhật thông tin người dùng: " + ex.getMessage());
        }
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
        LocalDate today = LocalDate.now(this.clock);
        LocalDate startOfMonth = today.withDayOfMonth(1);
        
        Date todayUtilDateStart = Date.from(today.atStartOfDay(this.clock.getZone()).toInstant());
        Date todayUtilDateEnd = Date.from(today.atTime(LocalTime.MAX).atZone(this.clock.getZone()).toInstant());
        Date startOfMonthUtilDate = Date.from(startOfMonth.atStartOfDay(this.clock.getZone()).toInstant());

        dto.setLuotTruyCapHomNay(dangNhapRepository.countByLoginTimeBetween(todayUtilDateStart, todayUtilDateEnd));
        dto.setLuotTruyCapThangNay(dangNhapRepository.countByLoginTimeBetween(startOfMonthUtilDate, todayUtilDateEnd));

        BigDecimal doanhThuHomNay = giaoDichRepository.sumSoTienByLoaiGiaoDichAndNgayThucHienBetween(TransactionType.DEPOSIT, today, today);
        dto.setDoanhThuHomNay(doanhThuHomNay != null ? doanhThuHomNay : BigDecimal.ZERO);
        
        BigDecimal doanhThuThangNay = giaoDichRepository.sumSoTienByLoaiGiaoDichAndNgayThucHienBetween(TransactionType.DEPOSIT, startOfMonth, today);
        dto.setDoanhThuThangNay(doanhThuThangNay != null ? doanhThuThangNay : BigDecimal.ZERO);
        
        dto.setTongSoNguoiDung(nguoiDungRepository.count());
        Long activeAccounts = moSoTietKiemRepository.countByTrangThai(MoSoTietKiem.TrangThaiMoSo.DANG_HOAT_DONG);
        dto.setTongSoTaiKhoanTietKiemDangHoatDong(activeAccounts != null ? activeAccounts : 0L);
        
        // >> LOGIC ĐÃ SỬA <<
        // Tính tổng số dư của các sổ đang hoạt động
        BigDecimal activeBalance = moSoTietKiemRepository.sumSoDuByTrangThai(MoSoTietKiem.TrangThaiMoSo.DANG_HOAT_DONG);
        // Tính tổng số dư của các sổ đã đáo hạn
        BigDecimal maturedBalance = moSoTietKiemRepository.sumSoDuByTrangThai(MoSoTietKiem.TrangThaiMoSo.DA_DAO_HAN);
        // Cộng hai giá trị lại để ra tổng số dư toàn hệ thống
        BigDecimal tongSoDuToanHeThong = (activeBalance != null ? activeBalance : BigDecimal.ZERO)
                                        .add(maturedBalance != null ? maturedBalance : BigDecimal.ZERO);
        dto.setTongSoDuToanHeThong(tongSoDuToanHeThong);

        Pageable limitRecent = PageRequest.of(0, 10, Sort.by("ngayThucHien").descending().and(Sort.by("id").descending()));
        Page<GiaoDichDTO> recentTransactionsPage = giaoDichService.getAllSystemTransactions(limitRecent, null, null, null, null);
        dto.setGiaoDichGanDayNhat(recentTransactionsPage.getContent());

        return dto;
    }

    @Transactional(readOnly = true)
    public Page<GiaoDichDTO> getAllSystemTransactionsPaginated(Pageable pageable) {
        return giaoDichService.getAllSystemTransactions(pageable, null, null, null, null);
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
        try {
            dangNhapRepository.deleteByNguoiDung(userToDelete);
            List<MoSoTietKiem> userAccounts = moSoTietKiemRepository.findByNguoiDung(userToDelete);
            for (MoSoTietKiem account : userAccounts) {
                moSoTietKiemRepository.delete(account);
            }
            nguoiDungRepository.delete(userToDelete);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new IllegalStateException("Không thể xóa người dùng do còn dữ liệu liên quan hoặc ràng buộc hệ thống.");
        } catch (Exception ex) {
            throw new RuntimeException("Lỗi khi xóa người dùng: " + ex.getMessage());
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