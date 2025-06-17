package com.SE104.quan_ly_so_tiet_kiem.service;

import com.SE104.quan_ly_so_tiet_kiem.dto.DailyReportDTO;
import com.SE104.quan_ly_so_tiet_kiem.dto.MonthlyReportDTO;
import com.SE104.quan_ly_so_tiet_kiem.dto.TransactionReportDTO;
import com.SE104.quan_ly_so_tiet_kiem.entity.GiaoDich;
import com.SE104.quan_ly_so_tiet_kiem.entity.MoSoTietKiem;
import com.SE104.quan_ly_so_tiet_kiem.entity.SoTietKiem;
import com.SE104.quan_ly_so_tiet_kiem.model.TransactionType;
import com.SE104.quan_ly_so_tiet_kiem.repository.GiaoDichRepository;
import com.SE104.quan_ly_so_tiet_kiem.repository.MoSoTietKiemRepository;
import com.SE104.quan_ly_so_tiet_kiem.repository.SoTietKiemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final GiaoDichRepository giaoDichRepository;
    private final MoSoTietKiemRepository moSoTietKiemRepository;
    private final SoTietKiemRepository soTietKiemRepository;
    private final PDFReportService pdfReportService;
    private final ExcelReportService excelReportService;

    @Autowired
    public ReportService(GiaoDichRepository giaoDichRepository,
                        MoSoTietKiemRepository moSoTietKiemRepository,
                        SoTietKiemRepository soTietKiemRepository,
                        PDFReportService pdfReportService, 
                        ExcelReportService excelReportService) {
        this.giaoDichRepository = giaoDichRepository;
        this.moSoTietKiemRepository = moSoTietKiemRepository;
        this.soTietKiemRepository = soTietKiemRepository;
        this.pdfReportService = pdfReportService;
        this.excelReportService = excelReportService;
    }    public List<TransactionReportDTO> getTransactionReport(LocalDate fromDate, LocalDate toDate) {
        List<GiaoDich> transactions = giaoDichRepository.findByNgayThucHienBetweenOrderByNgayThucHienDesc(fromDate, toDate);
        
        return transactions.stream()
                .map(this::convertToReportDTO)
                .collect(Collectors.toList());
    }

    public List<TransactionReportDTO> getTransactionReportByUser(Integer userId, LocalDate fromDate, LocalDate toDate) {
        List<GiaoDich> transactions = giaoDichRepository.findByMoSoTietKiem_NguoiDung_MaNDAndNgayThucHienBetweenOrderByNgayThucHienDesc(
                userId, fromDate, toDate);
        
        return transactions.stream()
                .map(this::convertToReportDTO)
                .collect(Collectors.toList());
    }

    public byte[] generatePDFReport(LocalDate fromDate, LocalDate toDate) {
        List<TransactionReportDTO> transactions = getTransactionReport(fromDate, toDate);
        return pdfReportService.generateTransactionReport(transactions, fromDate, toDate);
    }

    public byte[] generateExcelReport(LocalDate fromDate, LocalDate toDate) {
        List<TransactionReportDTO> transactions = getTransactionReport(fromDate, toDate);
        return excelReportService.generateTransactionReport(transactions, fromDate, toDate);
    }

    public byte[] generateUserPDFReport(Integer userId, LocalDate fromDate, LocalDate toDate) {
        List<TransactionReportDTO> transactions = getTransactionReportByUser(userId, fromDate, toDate);
        return pdfReportService.generateTransactionReport(transactions, fromDate, toDate);
    }

    public byte[] generateUserExcelReport(Integer userId, LocalDate fromDate, LocalDate toDate) {
        List<TransactionReportDTO> transactions = getTransactionReportByUser(userId, fromDate, toDate);
        return excelReportService.generateTransactionReport(transactions, fromDate, toDate);
    }

    /**
     * BM5.1 - Báo cáo doanh số hoạt động ngày
     */
    public List<DailyReportDTO> getDailyReport(LocalDate reportDate) {
        List<SoTietKiem> allProducts = soTietKiemRepository.findAll();
        AtomicInteger stt = new AtomicInteger(1);
        
        return allProducts.stream()
                .map(product -> {
                    // Tổng thu: Tổng tiền gửi vào sản phẩm này trong ngày
                    List<GiaoDich> deposits = giaoDichRepository.findByNgayThucHienAndLoaiGiaoDich(reportDate, TransactionType.DEPOSIT);
                    BigDecimal tongThu = deposits.stream()
                            .filter(gd -> gd.getMoSoTietKiem().getSoTietKiemSanPham().getMaSo().equals(product.getMaSo()))
                            .map(GiaoDich::getSoTien)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    // Tổng chi: Tổng lãi phải trả (từ các giao dịch rút tiền)
                    List<GiaoDich> withdrawals = giaoDichRepository.findByNgayThucHienAndLoaiGiaoDich(reportDate, TransactionType.WITHDRAW);
                    BigDecimal tongChi = withdrawals.stream()
                            .filter(gd -> gd.getMoSoTietKiem().getSoTietKiemSanPham().getMaSo().equals(product.getMaSo()))
                            .map(gd -> {
                                // Lấy tiền lãi từ phiếu rút tiền tương ứng
                                return gd.getMoSoTietKiem().getPhieuRutTienList().stream()
                                        .filter(prt -> prt.getNgayRut() != null && 
                                               prt.getNgayRut().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate().equals(reportDate))
                                        .map(prt -> prt.getTienLaiThucNhan() != null ? prt.getTienLaiThucNhan() : BigDecimal.ZERO)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                            })
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    BigDecimal chenhLech = tongThu.subtract(tongChi);
                    
                    return new DailyReportDTO(
                            stt.getAndIncrement(),
                            product.getTenSo(),
                            tongThu,
                            tongChi,
                            chenhLech
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * BM5.2 - Báo cáo mở/đóng sổ tháng
     */
    public List<MonthlyReportDTO> getMonthlyReport(LocalDate fromDate, LocalDate toDate) {
        AtomicInteger stt = new AtomicInteger(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        return fromDate.datesUntil(toDate.plusDays(1))
                .map(date -> {                    // Số sổ mở trong ngày (đếm theo ngày mở)
                    Long soSoMoLong = moSoTietKiemRepository.countByNgayMo(date);
                    Integer soSoMo = soSoMoLong != null ? soSoMoLong.intValue() : 0;
                    
                    // Số sổ đóng trong ngày (tạm thời để 0 - cần implement logic đếm theo ngày đóng)
                    Integer soSoDong = 0; // TODO: Implement counting closed accounts by date
                      return new MonthlyReportDTO(
                            stt.getAndIncrement(),
                            date.format(formatter),
                            soSoMo,
                            soSoDong,
                            soSoMo - soSoDong  // Chênh lệch là số lượng (Integer)
                    );
                })
                .collect(Collectors.toList());
    }

    public byte[] generateDailyReportPDF(LocalDate reportDate) {
        List<DailyReportDTO> dailyReport = getDailyReport(reportDate);
        return pdfReportService.generateDailyReport(dailyReport, reportDate);
    }

    public byte[] generateDailyReportExcel(LocalDate reportDate) {
        List<DailyReportDTO> dailyReport = getDailyReport(reportDate);
        return excelReportService.generateDailyReport(dailyReport, reportDate);
    }

    public byte[] generateMonthlyReportPDF(LocalDate fromDate, LocalDate toDate) {
        List<MonthlyReportDTO> monthlyReport = getMonthlyReport(fromDate, toDate);
        return pdfReportService.generateMonthlyReport(monthlyReport, fromDate, toDate);
    }

    public byte[] generateMonthlyReportExcel(LocalDate fromDate, LocalDate toDate) {
        List<MonthlyReportDTO> monthlyReport = getMonthlyReport(fromDate, toDate);
        return excelReportService.generateMonthlyReport(monthlyReport, fromDate, toDate);
    }

    private TransactionReportDTO convertToReportDTO(GiaoDich giaoDich) {
        return new TransactionReportDTO(
                giaoDich.getId().intValue(),
                giaoDich.getNgayThucHien(),
                getTransactionTypeDisplay(giaoDich.getLoaiGiaoDich()),
                giaoDich.getSoTien(),
                "", // Description - GiaoDich doesn't have description field
                giaoDich.getMoSoTietKiem().getMaMoSo(),
                giaoDich.getMoSoTietKiem().getTenSoMo(),
                giaoDich.getMoSoTietKiem().getNguoiDung().getTenND(),
                giaoDich.getMoSoTietKiem().getSoDu() // Current balance, not balance after transaction
        );
    }

    private String getTransactionTypeDisplay(com.SE104.quan_ly_so_tiet_kiem.model.TransactionType transactionType) {
        switch (transactionType) {
            case DEPOSIT:
                return "Gửi tiền";
            case WITHDRAW:
                return "Rút tiền";
            case INTEREST:
                return "Trả lãi";
            default:
                return transactionType.toString();
        }
    }
}
