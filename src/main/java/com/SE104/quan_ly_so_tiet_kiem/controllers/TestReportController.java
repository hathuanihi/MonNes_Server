package com.SE104.quan_ly_so_tiet_kiem.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.SE104.quan_ly_so_tiet_kiem.service.ReportService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/test")
public class TestReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/report-pdf")
    public String testPDFGeneration() {
        try {
            LocalDate fromDate = LocalDate.of(2025, 1, 1);
            LocalDate toDate = LocalDate.now();
            
            byte[] pdf = reportService.generatePDFReport(fromDate, toDate);
            return "PDF generated successfully. Size: " + pdf.length + " bytes";
        } catch (Exception e) {
            return "Error generating PDF: " + e.getMessage();
        }
    }

    @GetMapping("/report-excel")
    public String testExcelGeneration() {
        try {
            LocalDate fromDate = LocalDate.of(2025, 1, 1);
            LocalDate toDate = LocalDate.now();
            
            byte[] excel = reportService.generateExcelReport(fromDate, toDate);
            return "Excel generated successfully. Size: " + excel.length + " bytes";
        } catch (Exception e) {
            return "Error generating Excel: " + e.getMessage();
        }
    }
}
