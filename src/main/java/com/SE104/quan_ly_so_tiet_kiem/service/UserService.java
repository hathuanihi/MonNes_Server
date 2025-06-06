package com.SE104.quan_ly_so_tiet_kiem.service;

import com.SE104.quan_ly_so_tiet_kiem.dto.*;
import com.SE104.quan_ly_so_tiet_kiem.entity.GiaoDich;
import com.SE104.quan_ly_so_tiet_kiem.entity.MoSoTietKiem;
import com.SE104.quan_ly_so_tiet_kiem.entity.NguoiDung;
import com.SE104.quan_ly_so_tiet_kiem.model.TransactionType;
import com.SE104.quan_ly_so_tiet_kiem.repository.GiaoDichRepository;
import com.SE104.quan_ly_so_tiet_kiem.repository.MoSoTietKiemRepository;
import com.SE104.quan_ly_so_tiet_kiem.repository.NguoiDungRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private MoSoTietKiemRepository moSoTietKiemRepository;

    @Autowired
    private GiaoDichRepository giaoDichRepository;

    @Autowired 
    private MoSoTietKiemService moSoTietKiemService;

    @Autowired 
    private GiaoDichService giaoDichService;


    @Transactional(readOnly = true)
    public UserResponse getUserProfileByEmail(String email) {
        NguoiDung nguoiDung = nguoiDungRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với email: " + email));
        return mapToUserResponse(nguoiDung);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserProfileById(Integer userId) {
        NguoiDung nguoiDung = nguoiDungRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với ID: " + userId));
        return mapToUserResponse(nguoiDung);
    }

    @Transactional(readOnly = true)
    public NguoiDung getNguoiDungEntityById(Integer userId) {
        return nguoiDungRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với ID: " + userId));
    }


    @Transactional
    public UserResponse updateUserProfile(Integer userId, UpdateProfileDTO profileDTO) {
        NguoiDung user = nguoiDungRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        if (profileDTO.getEmail() != null && !profileDTO.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (nguoiDungRepository.existsByEmail(profileDTO.getEmail())) {
                throw new IllegalArgumentException("Email '" + profileDTO.getEmail() + "' đã được sử dụng bởi tài khoản khác.");
            }
            user.setEmail(profileDTO.getEmail());
        }

        if (profileDTO.getSdt() != null && !profileDTO.getSdt().equals(user.getSdt())) {
            if (nguoiDungRepository.existsBySdt(profileDTO.getSdt())) {
                throw new IllegalArgumentException("Số điện thoại '" + profileDTO.getSdt() + "' đã được sử dụng bởi tài khoản khác.");
            }
            user.setSdt(profileDTO.getSdt());
        }

        if (profileDTO.getCccd() != null && !profileDTO.getCccd().isEmpty() && 
            (user.getCccd() == null || !profileDTO.getCccd().equals(user.getCccd()))) {
            user.setCccd(profileDTO.getCccd());
        }


        if (profileDTO.getTenND() != null) user.setTenND(profileDTO.getTenND());
        if (profileDTO.getDiaChi() != null) user.setDiaChi(profileDTO.getDiaChi());
        
        if (profileDTO.getNgaySinh() != null) {

            user.setNgaySinh(new java.util.Date(profileDTO.getNgaySinh().getTime()));
        } else {
            user.setNgaySinh(null); 
        }

        NguoiDung updatedUser = nguoiDungRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    public UserResponse mapToUserResponse(NguoiDung user) {
        if (user == null) return null;
        UserResponse response = new UserResponse();
        response.setId(user.getMaND());
        response.setTenND(user.getTenND());
        response.setCccd(user.getCccd());
        response.setDiaChi(user.getDiaChi());
        response.setSdt(user.getSdt());
        response.setEmail(user.getEmail());
        if (user.getNgaySinh() != null) {
            response.setNgaySinh(new java.sql.Date(user.getNgaySinh().getTime()));
        } else {
            response.setNgaySinh(null);
        }
        response.setVaiTro(user.getVaiTro() == 0 ? "ADMIN" : "USER");
        return response;
    }

   @Transactional(readOnly = true)
    public UserAccountSummaryDTO getUserAccountSummary(Integer userId) {
        if (!nguoiDungRepository.existsById(userId)) {
            throw new EntityNotFoundException("Người dùng không tồn tại với ID: " + userId);
        }

        UserAccountSummaryDTO summary = new UserAccountSummaryDTO();
        // Lấy tất cả các sổ của người dùng một lần để tối ưu
        List<MoSoTietKiem> accounts = moSoTietKiemRepository.findByNguoiDung_MaND(userId);

        // >> LOGIC ĐÃ SỬA <<
        // Tính tổng số dư từ các sổ "Đang hoạt động" VÀ "Đã đáo hạn"
        BigDecimal tongSoDu = accounts.stream()
                .filter(acc -> acc.getTrangThai() == MoSoTietKiem.TrangThaiMoSo.DANG_HOAT_DONG ||
                            acc.getTrangThai() == MoSoTietKiem.TrangThaiMoSo.DA_DAO_HAN)
                .map(MoSoTietKiem::getSoDu)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setTongSoDuTrongTatCaSo(tongSoDu);

        // Các phần còn lại giữ nguyên vì đã đúng
        List<GiaoDich> allUserTransactions = giaoDichRepository.findByMoSoTietKiem_NguoiDung_MaND(userId);

        BigDecimal tongTienNap = allUserTransactions.stream()
                .filter(gd -> gd.getLoaiGiaoDich() == TransactionType.DEPOSIT)
                .map(GiaoDich::getSoTien)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setTongTienDaNapTuTruocDenNay(tongTienNap);

        BigDecimal tongTienRut = allUserTransactions.stream()
                .filter(gd -> gd.getLoaiGiaoDich() == TransactionType.WITHDRAW)
                .map(GiaoDich::getSoTien)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setTongTienDaRutTuTruocDenNay(tongTienRut);

        summary.setTongSoLuongSoTietKiemDaMo(accounts.size());
        summary.setTongSoGiaoDichDaThucHien(allUserTransactions.size());

        return summary;
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getUserDashboardData(Integer userId) {
        Map<String, Object> dashboardData = new HashMap<>();

        dashboardData.put("accountSummary", getUserAccountSummary(userId));

        List<MoSoTietKiemResponse> activeAccounts = moSoTietKiemRepository.findByNguoiDung_MaNDAndTrangThai(userId, MoSoTietKiem.TrangThaiMoSo.DANG_HOAT_DONG)
                .stream()
                .map(moSoTietKiemService::mapEntityToResponse)
                .collect(Collectors.toList());
        dashboardData.put("activeSavingsAccounts", activeAccounts);
        
        dashboardData.put("recentTransactions", giaoDichService.getRecentTransactionsForUser(userId, 5));

        return dashboardData;
    }
}