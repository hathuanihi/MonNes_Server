package com.SE104.quan_ly_so_tiet_kiem.controllers;

import com.SE104.quan_ly_so_tiet_kiem.dto.DailyReportDTO;
import com.SE104.quan_ly_so_tiet_kiem.dto.MonthlyReportDTO;
import com.SE104.quan_ly_so_tiet_kiem.dto.TransactionReportDTO;
import com.SE104.quan_ly_so_tiet_kiem.security.CustomUserDetails;
import com.SE104.quan_ly_so_tiet_kiem.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "API for generating reports")
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);
    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get transaction report data", description = "Get transaction report data for admin")
    public ResponseEntity<List<TransactionReportDTO>> getTransactionReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        logger.info("Admin requesting transaction report from {} to {}", fromDate, toDate);
        List<TransactionReportDTO> report = reportService.getTransactionReport(fromDate, toDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/transactions/user")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Get user transaction report data", description = "Get transaction report data for current user")
    public ResponseEntity<List<TransactionReportDTO>> getUserTransactionReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
          logger.info("User {} requesting transaction report from {} to {}", 
                userDetails.getUsername(), fromDate, toDate);
        List<TransactionReportDTO> report = reportService.getTransactionReportByUser(
                userDetails.getMaND(), fromDate, toDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/transactions/export/pdf")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Export transaction report to PDF", description = "Generate PDF report for admin")
    public ResponseEntity<byte[]> exportTransactionReportToPDF(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        logger.info("Admin exporting transaction report to PDF from {} to {}", fromDate, toDate);
        
        try {
            byte[] pdfBytes = reportService.generatePDFReport(fromDate, toDate);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                    generateFileName("transaction-report", fromDate, toDate, "pdf"));
            headers.setContentLength(pdfBytes.length);
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error generating PDF report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/transactions/export/excel")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Export transaction report to Excel", description = "Generate Excel report for admin")
    public ResponseEntity<byte[]> exportTransactionReportToExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        logger.info("Admin exporting transaction report to Excel from {} to {}", fromDate, toDate);
        
        try {
            byte[] excelBytes = reportService.generateExcelReport(fromDate, toDate);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                    generateFileName("transaction-report", fromDate, toDate, "xlsx"));
            headers.setContentLength(excelBytes.length);
            
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error generating Excel report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/transactions/user/export/pdf")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Export user transaction report to PDF", description = "Generate PDF report for current user")
    public ResponseEntity<byte[]> exportUserTransactionReportToPDF(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        logger.info("User {} exporting transaction report to PDF from {} to {}", 
                userDetails.getUsername(), fromDate, toDate);
        
        try {
            byte[] pdfBytes = reportService.generateUserPDFReport(userDetails.getMaND(), fromDate, toDate);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                    generateFileName("my-transactions", fromDate, toDate, "pdf"));
            headers.setContentLength(pdfBytes.length);
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error generating user PDF report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/transactions/user/export/excel")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Export user transaction report to Excel", description = "Generate Excel report for current user")
    public ResponseEntity<byte[]> exportUserTransactionReportToExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        logger.info("User {} exporting transaction report to Excel from {} to {}", 
                userDetails.getUsername(), fromDate, toDate);
        
        try {
            byte[] excelBytes = reportService.generateUserExcelReport(userDetails.getMaND(), fromDate, toDate);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                    generateFileName("my-transactions", fromDate, toDate, "xlsx"));
            headers.setContentLength(excelBytes.length);
            
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error generating user Excel report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== BM5.1 - Báo cáo doanh số hoạt động ngày ==========
    
    @GetMapping("/daily")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get daily report data (BM5.1)", description = "Get daily activity report for admin")
    public ResponseEntity<List<DailyReportDTO>> getDailyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reportDate) {
        
        logger.info("Admin requesting daily report for {}", reportDate);
        List<DailyReportDTO> report = reportService.getDailyReport(reportDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/daily/export/pdf")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Export daily report to PDF (BM5.1)", description = "Generate PDF daily report for admin")
    public ResponseEntity<byte[]> exportDailyReportToPDF(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reportDate) {
        
        logger.info("Admin exporting daily report to PDF for {}", reportDate);
        
        try {
            byte[] pdfBytes = reportService.generateDailyReportPDF(reportDate);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                    generateFileName("daily-report", reportDate, reportDate, "pdf"));
            headers.setContentLength(pdfBytes.length);
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error generating daily PDF report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/daily/export/excel")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Export daily report to Excel (BM5.1)", description = "Generate Excel daily report for admin")
    public ResponseEntity<byte[]> exportDailyReportToExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reportDate) {
        
        logger.info("Admin exporting daily report to Excel for {}", reportDate);
        
        try {
            byte[] excelBytes = reportService.generateDailyReportExcel(reportDate);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                    generateFileName("daily-report", reportDate, reportDate, "xlsx"));
            headers.setContentLength(excelBytes.length);
            
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error generating daily Excel report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== BM5.2 - Báo cáo mở/đóng sổ tháng ==========
    
    @GetMapping("/monthly")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get monthly report data (BM5.2)", description = "Get monthly open/close report for admin")
    public ResponseEntity<List<MonthlyReportDTO>> getMonthlyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        logger.info("Admin requesting monthly report from {} to {}", fromDate, toDate);
        List<MonthlyReportDTO> report = reportService.getMonthlyReport(fromDate, toDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/monthly/export/pdf")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Export monthly report to PDF (BM5.2)", description = "Generate PDF monthly report for admin")
    public ResponseEntity<byte[]> exportMonthlyReportToPDF(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        logger.info("Admin exporting monthly report to PDF from {} to {}", fromDate, toDate);
        
        try {
            byte[] pdfBytes = reportService.generateMonthlyReportPDF(fromDate, toDate);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                    generateFileName("monthly-report", fromDate, toDate, "pdf"));
            headers.setContentLength(pdfBytes.length);
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error generating monthly PDF report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/monthly/export/excel")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Export monthly report to Excel (BM5.2)", description = "Generate Excel monthly report for admin")
    public ResponseEntity<byte[]> exportMonthlyReportToExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        logger.info("Admin exporting monthly report to Excel from {} to {}", fromDate, toDate);
        
        try {
            byte[] excelBytes = reportService.generateMonthlyReportExcel(fromDate, toDate);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                    generateFileName("monthly-report", fromDate, toDate, "xlsx"));
            headers.setContentLength(excelBytes.length);
            
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error generating monthly Excel report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String generateFileName(String prefix, LocalDate fromDate, LocalDate toDate, String extension) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return String.format("%s_%s_to_%s.%s", 
                prefix, 
                fromDate.format(formatter), 
                toDate.format(formatter), 
                extension);
    }

    private String generateDailyFileName(String prefix, LocalDate reportDate, String extension) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return String.format("%s_%s.%s", 
                prefix, 
                reportDate.format(formatter), 
                extension);
    }
}
