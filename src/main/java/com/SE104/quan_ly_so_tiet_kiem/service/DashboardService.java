package com.SE104.quan_ly_so_tiet_kiem.service;

import com.SE104.quan_ly_so_tiet_kiem.dto.DashboardSummaryDTO;
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
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

@Service
public class DashboardService {

    @Autowired
    private GiaoDichRepository giaoDichRepository;
    @Autowired
    private NguoiDungRepository nguoiDungRepository;
    @Autowired
    private MoSoTietKiemRepository moSoTietKiemRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryDTO getMonthlySummaryForUser(String email) {
        NguoiDung nguoiDung = nguoiDungRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với email: " + email));

        // Lấy TẤT CẢ các sổ của người dùng để xử lý, thay vì chỉ lấy sổ đang hoạt động
        List<MoSoTietKiem> allUserAccounts = moSoTietKiemRepository.findByNguoiDung_MaND(nguoiDung.getMaND());

        // >> LOGIC ĐÃ SỬA <<
        // Tính tổng số dư từ các sổ đang hoạt động VÀ đã đáo hạn.
        BigDecimal currentTotalBalance = allUserAccounts.stream()
                .filter(acc -> acc.getTrangThai() == MoSoTietKiem.TrangThaiMoSo.DANG_HOAT_DONG ||
                               acc.getTrangThai() == MoSoTietKiem.TrangThaiMoSo.DA_DAO_HAN)
                .map(MoSoTietKiem::getSoDu)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // >> TỐI ƯU HÓA <<
        // Đếm số tài khoản đang hoạt động từ danh sách đã lấy ở trên.
        Integer activeAccountsCount = (int) allUserAccounts.stream()
                .filter(acc -> acc.getTrangThai() == MoSoTietKiem.TrangThaiMoSo.DANG_HOAT_DONG)
                .count();

        YearMonth currentMonth = YearMonth.now();
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();

        BigDecimal depositThisMonth = giaoDichRepository.sumSoTienByNguoiDungAndLoaiGiaoDichAndDateRange(
                nguoiDung.getMaND(),
                TransactionType.DEPOSIT,
                startDate,
                endDate);
        depositThisMonth = (depositThisMonth == null) ? BigDecimal.ZERO : depositThisMonth;

        BigDecimal withdrawThisMonth = giaoDichRepository.sumSoTienByNguoiDungAndLoaiGiaoDichAndDateRange(
                nguoiDung.getMaND(),
                TransactionType.WITHDRAW,
                startDate,
                endDate);
        withdrawThisMonth = (withdrawThisMonth == null) ? BigDecimal.ZERO : withdrawThisMonth;

        DashboardSummaryDTO summaryDTO = new DashboardSummaryDTO();
        summaryDTO.setTongSoDuTatCaSoCuaUser(currentTotalBalance);
        summaryDTO.setTongTienDaNapThangNay(depositThisMonth);
        summaryDTO.setTongTienDaRutThangNay(withdrawThisMonth);
        summaryDTO.setSoLuongSoTietKiemDangHoatDong(activeAccountsCount);

        return summaryDTO;
    }

    /**
     * Lấy dữ liệu thống kê toàn hệ thống, tái sử dụng DashboardSummaryDTO.
     * @param today Ngày cần thống kê.
     * @return DTO chứa thông tin thống kê.
     */
    @Transactional(readOnly = true)
    public DashboardSummaryDTO getSystemWideSummary(LocalDate today) {
        YearMonth currentMonth = YearMonth.from(today);
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();

        // Tổng số dư toàn hệ thống
        // Logic này đã đúng vì nó findAll() rồi cộng dồn, không bị ảnh hưởng bởi lỗi trên.
        BigDecimal totalSystemBalance = moSoTietKiemRepository.findAll().stream()
                .map(MoSoTietKiem::getSoDu)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tổng tiền nạp trong tháng trên toàn hệ thống
        BigDecimal totalDepositsThisMonth = giaoDichRepository.sumSoTienByLoaiGiaoDichAndDateRange(
            TransactionType.DEPOSIT, startDate, endDate);
        totalDepositsThisMonth = (totalDepositsThisMonth == null) ? BigDecimal.ZERO : totalDepositsThisMonth;

        // Tổng tiền rút trong tháng trên toàn hệ thống
        BigDecimal totalWithdrawalsThisMonth = giaoDichRepository.sumSoTienByLoaiGiaoDichAndDateRange(
            TransactionType.WITHDRAW, startDate, endDate);
        totalWithdrawalsThisMonth = (totalWithdrawalsThisMonth == null) ? BigDecimal.ZERO : totalWithdrawalsThisMonth;
            
        // Tổng số sổ đang hoạt động trên toàn hệ thống
        Long totalActiveAccounts = moSoTietKiemRepository.countByTrangThai(MoSoTietKiem.TrangThaiMoSo.DANG_HOAT_DONG);
        totalActiveAccounts = (totalActiveAccounts == null) ? 0L : totalActiveAccounts;

        DashboardSummaryDTO summaryDTO = new DashboardSummaryDTO();
        // Ánh xạ dữ liệu hệ thống vào các trường DTO có sẵn
        summaryDTO.setTongSoDuTatCaSoCuaUser(totalSystemBalance); // Tái sử dụng trường này cho tổng số dư hệ thống
        summaryDTO.setTongTienDaNapThangNay(totalDepositsThisMonth); // Trường này phù hợp
        summaryDTO.setTongTienDaRutThangNay(totalWithdrawalsThisMonth); // Trường này phù hợp
        summaryDTO.setSoLuongSoTietKiemDangHoatDong(totalActiveAccounts.intValue()); // Tái sử dụng trường này cho tổng số sổ hoạt động

        return summaryDTO;
    }
}