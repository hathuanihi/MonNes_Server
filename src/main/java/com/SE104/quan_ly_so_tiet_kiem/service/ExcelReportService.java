package com.SE104.quan_ly_so_tiet_kiem.service;

import com.SE104.quan_ly_so_tiet_kiem.dto.TransactionReportDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelReportService {

    public byte[] generateTransactionReport(List<TransactionReportDTO> transactions, LocalDate fromDate, LocalDate toDate) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Báo cáo giao dịch");
            
            // Create styles
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle totalStyle = createTotalStyle(workbook);

            int rowNum = 0;            // Title
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("BÁO CÁO GIAO DỊCH");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));            // Date range
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            Row dateRow = sheet.createRow(rowNum++);
            Cell dateCell = dateRow.createCell(0);
            dateCell.setCellValue(String.format("Từ ngày: %s đến ngày: %s", 
                    fromDate.format(formatter), toDate.format(formatter)));
            dateCell.setCellStyle(dataStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 5));

            // Empty row
            rowNum++;            // Headers - Bỏ cột mô tả
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"STT", "Ngày GD", "Loại GD", "Số tiền", "Mã số", "Khách hàng"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int index = 1;
            BigDecimal totalAmount = BigDecimal.ZERO;
            
            for (TransactionReportDTO transaction : transactions) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(index++);
                row.getCell(0).setCellStyle(dataStyle);
                
                row.createCell(1).setCellValue(transaction.getTransactionDate().format(formatter));
                row.getCell(1).setCellStyle(dataStyle);
                
                row.createCell(2).setCellValue(transaction.getTransactionType());
                row.getCell(2).setCellStyle(dataStyle);
                
                Cell amountCell = row.createCell(3);
                amountCell.setCellValue(transaction.getAmount().doubleValue());
                amountCell.setCellStyle(currencyStyle);
                
                row.createCell(4).setCellValue(transaction.getAccountId());
                row.getCell(4).setCellStyle(dataStyle);
                
                row.createCell(5).setCellValue(transaction.getCustomerName());
                row.getCell(5).setCellStyle(dataStyle);
                
                totalAmount = totalAmount.add(transaction.getAmount());
            }            // Total row
            Row totalRow = sheet.createRow(rowNum++);
            Cell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue("TỔNG CỘNG");
            totalLabelCell.setCellStyle(totalStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 2));
            
            Cell totalAmountCell = totalRow.createCell(3);
            totalAmountCell.setCellValue(totalAmount.doubleValue());
            totalAmountCell.setCellStyle(totalStyle);

            // Empty row
            rowNum++;            // Summary
            Row summaryRow1 = sheet.createRow(rowNum++);
            summaryRow1.createCell(0).setCellValue(String.format("Tổng số giao dịch: %d", transactions.size()));
            summaryRow1.getCell(0).setCellStyle(dataStyle);

            Row summaryRow2 = sheet.createRow(rowNum++);
            summaryRow2.createCell(0).setCellValue(String.format("Báo cáo được tạo vào: %s", LocalDate.now().format(formatter)));
            summaryRow2.getCell(0).setCellStyle(dataStyle);

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating Excel report", e);
        }
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private CellStyle createTotalStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }
}
