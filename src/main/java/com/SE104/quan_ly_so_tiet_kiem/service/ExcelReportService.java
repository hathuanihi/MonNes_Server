package com.SE104.quan_ly_so_tiet_kiem.service;

import com.SE104.quan_ly_so_tiet_kiem.dto.DailyReportDTO;
import com.SE104.quan_ly_so_tiet_kiem.dto.MonthlyReportDTO;
import com.SE104.quan_ly_so_tiet_kiem.dto.TransactionReportDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelReportService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExcelReportService.class);

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

            int rowNum = 0;           
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
            rowNum++;           
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

    /**
     * Generate BM5.1 - Báo cáo doanh số hoạt động ngày
     */
    public byte[] generateDailyReport(List<DailyReportDTO> reportData, LocalDate reportDate) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Báo cáo doanh số hoạt động ngày");
            
            // Title
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("BM5.1 - Báo Cáo Doanh Số Hoạt Động Ngày");
            titleCell.setCellStyle(createTitleStyle(workbook));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));
            
            // Date
            Row dateRow = sheet.createRow(1);
            Cell dateCell = dateRow.createCell(0);
            dateCell.setCellValue("Ngày: " + reportDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            dateCell.setCellStyle(createDateStyle(workbook));
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 4));
            
            // Headers
            Row headerRow = sheet.createRow(3);
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            String[] headers = {"STT", "Loại Tiết Kiệm", "Tổng Thu", "Tổng Chi", "Chênh Lệch"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Data rows
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            
            int rowIndex = 4;
            for (DailyReportDTO item : reportData) {
                Row row = sheet.createRow(rowIndex++);
                
                Cell sttCell = row.createCell(0);
                sttCell.setCellValue(item.getStt());
                sttCell.setCellStyle(dataStyle);
                
                Cell loaiTKCell = row.createCell(1);
                loaiTKCell.setCellValue(item.getLoaiTietKiem());
                loaiTKCell.setCellStyle(dataStyle);
                
                Cell tongThuCell = row.createCell(2);
                tongThuCell.setCellValue(item.getTongThu().doubleValue());
                tongThuCell.setCellStyle(currencyStyle);
                
                Cell tongChiCell = row.createCell(3);
                tongChiCell.setCellValue(item.getTongChi().doubleValue());
                tongChiCell.setCellStyle(currencyStyle);
                
                Cell chenhLechCell = row.createCell(4);
                chenhLechCell.setCellValue(item.getChenhLech().doubleValue());
                chenhLechCell.setCellStyle(currencyStyle);
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(baos);
            return baos.toByteArray();
            
        } catch (Exception e) {
            logger.error("Error generating daily report Excel", e);
            throw new RuntimeException("Error generating daily report Excel", e);
        }
    }

    /**
     * Generate BM5.2 - Báo cáo mở/đóng sổ tháng
     */
    public byte[] generateMonthlyReport(List<MonthlyReportDTO> reportData, LocalDate fromDate, LocalDate toDate) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Báo cáo mở-đóng sổ tháng");
            
            // Title
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("BM5.2 - Báo Cáo Mở/Đóng Sổ Tháng");
            titleCell.setCellStyle(createTitleStyle(workbook));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));
            
            // Date range
            Row dateRow = sheet.createRow(1);
            Cell dateCell = dateRow.createCell(0);
            dateCell.setCellValue("Từ ngày: " + fromDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                               " đến ngày: " + toDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            dateCell.setCellStyle(createDateStyle(workbook));
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 4));
            
            // Headers
            Row headerRow = sheet.createRow(3);
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            String[] headers = {"STT", "Ngày", "Số Sổ Mở", "Số Sổ Đóng", "Chênh Lệch"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Data rows
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            
            int rowIndex = 4;
            for (MonthlyReportDTO item : reportData) {
                Row row = sheet.createRow(rowIndex++);
                
                Cell sttCell = row.createCell(0);
                sttCell.setCellValue(item.getStt());
                sttCell.setCellStyle(dataStyle);
                
                Cell ngayCell = row.createCell(1);
                ngayCell.setCellValue(item.getNgay());
                ngayCell.setCellStyle(dataStyle);
                
                Cell soSoMoCell = row.createCell(2);
                soSoMoCell.setCellValue(item.getSoSoMo());
                soSoMoCell.setCellStyle(numberStyle);
                
                Cell soSoDongCell = row.createCell(3);
                soSoDongCell.setCellValue(item.getSoSoDong());
                soSoDongCell.setCellStyle(numberStyle);                Cell chenhLechCell = row.createCell(4);
                chenhLechCell.setCellValue(item.getChenhLech()); // Số lượng (Integer)
                chenhLechCell.setCellStyle(numberStyle);
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(baos);
            return baos.toByteArray();
            
        } catch (Exception e) {
            logger.error("Error generating monthly report Excel", e);
            throw new RuntimeException("Error generating monthly report Excel", e);
        }
    }    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
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
    }    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0\" VND\""));
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
    }    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("0"));
        
        return style;
    }
}
