package com.SE104.quan_ly_so_tiet_kiem.controllers;

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

    private String generateFileName(String prefix, LocalDate fromDate, LocalDate toDate, String extension) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return String.format("%s_%s_to_%s.%s", 
                prefix, 
                fromDate.format(formatter), 
                toDate.format(formatter), 
                extension);
    }
}
