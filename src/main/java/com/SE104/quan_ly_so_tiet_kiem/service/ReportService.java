package com.SE104.quan_ly_so_tiet_kiem.service;

import com.SE104.quan_ly_so_tiet_kiem.dto.TransactionReportDTO;
import com.SE104.quan_ly_so_tiet_kiem.entity.GiaoDich;
import com.SE104.quan_ly_so_tiet_kiem.repository.GiaoDichRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final GiaoDichRepository giaoDichRepository;
    private final PDFReportService pdfReportService;
    private final ExcelReportService excelReportService;

    @Autowired
    public ReportService(GiaoDichRepository giaoDichRepository, 
                        PDFReportService pdfReportService, 
                        ExcelReportService excelReportService) {
        this.giaoDichRepository = giaoDichRepository;
        this.pdfReportService = pdfReportService;
        this.excelReportService = excelReportService;
    }

    public List<TransactionReportDTO> getTransactionReport(LocalDate fromDate, LocalDate toDate) {
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
    }    private TransactionReportDTO convertToReportDTO(GiaoDich giaoDich) {
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
