package com.SE104.quan_ly_so_tiet_kiem.service;

import com.SE104.quan_ly_so_tiet_kiem.dto.DailyReportDTO;
import com.SE104.quan_ly_so_tiet_kiem.dto.MonthlyReportDTO;
import com.SE104.quan_ly_so_tiet_kiem.dto.TransactionReportDTO;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.geom.PageSize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PDFReportService {
    
    private static final Logger logger = LoggerFactory.getLogger(PDFReportService.class);

    public byte[] generateTransactionReport(List<TransactionReportDTO> transactions, LocalDate fromDate, LocalDate toDate) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Font setup - Sử dụng font cơ bản để tránh lỗi encoding
            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            // Title
            Paragraph title = new Paragraph("BAO CAO GIAO DICH")
                    .setFont(boldFont)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);

            // Date range
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            Paragraph dateRange = new Paragraph(String.format("Tu ngay: %s den ngay: %s", 
                    fromDate.format(formatter), toDate.format(formatter)))
                    .setFont(font)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(dateRange);

            // Create table - 6 cột, tất cả đều center align
            Table table = new Table(UnitValue.createPercentArray(new float[]{8, 15, 15, 20, 12, 30}))
                    .setWidth(UnitValue.createPercentValue(100));

            // Table headers - Sử dụng text không dấu để tránh lỗi encoding
            String[] headers = {"STT", "Ngay GD", "Loai GD", "So tien", "Ma so", "Khach hang"};
            for (String header : headers) {
                Cell headerCell = new Cell()
                        .add(new Paragraph(header).setFont(boldFont).setFontSize(10))
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setTextAlignment(TextAlignment.CENTER);
                table.addHeaderCell(headerCell);
            }            // Add data rows - Tất cả đều center align
            int index = 1;
            BigDecimal totalAmount = BigDecimal.ZERO;
            
            for (TransactionReportDTO transaction : transactions) {
                // STT - center
                table.addCell(new Cell().add(new Paragraph(String.valueOf(index++)).setFont(font).setFontSize(9))
                        .setTextAlignment(TextAlignment.CENTER));
                
                // Ngày GD - center
                table.addCell(new Cell().add(new Paragraph(transaction.getTransactionDate().format(formatter)).setFont(font).setFontSize(9))
                        .setTextAlignment(TextAlignment.CENTER));
                
                // Loại GD - center (chuyển đổi tiếng Việt)
                table.addCell(new Cell().add(new Paragraph(removeVietnameseDiacritics(transaction.getTransactionType())).setFont(font).setFontSize(9))
                        .setTextAlignment(TextAlignment.CENTER));
                
                // Số tiền - center
                table.addCell(new Cell().add(new Paragraph(formatCurrency(transaction.getAmount())).setFont(font).setFontSize(9))
                        .setTextAlignment(TextAlignment.CENTER));
                
                // Mã số - center
                table.addCell(new Cell().add(new Paragraph(String.valueOf(transaction.getAccountId())).setFont(font).setFontSize(9))
                        .setTextAlignment(TextAlignment.CENTER));
                
                // Khách hàng - center (chuyển đổi tiếng Việt)
                table.addCell(new Cell().add(new Paragraph(removeVietnameseDiacritics(transaction.getCustomerName())).setFont(font).setFontSize(9))
                        .setTextAlignment(TextAlignment.CENTER));
                
                totalAmount = totalAmount.add(transaction.getAmount());
            }

            // Add total row - center align
            Cell totalLabelCell = new Cell(1, 3)
                    .add(new Paragraph("TONG CONG").setFont(boldFont).setFontSize(10))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY);
            table.addCell(totalLabelCell);
            
            Cell totalAmountCell = new Cell()
                    .add(new Paragraph(formatCurrency(totalAmount)).setFont(boldFont).setFontSize(10))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY);
            table.addCell(totalAmountCell);
            
            // Empty cells for remaining columns
            for (int i = 0; i < 2; i++) {
                table.addCell(new Cell().setBackgroundColor(ColorConstants.LIGHT_GRAY));
            }

            document.add(table);

            // Summary - center align
            Paragraph summary = new Paragraph(String.format("\nTong so giao dich: %d\nTong gia tri: %s", 
                    transactions.size(), formatCurrency(totalAmount)))
                    .setFont(font)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(20);
            document.add(summary);

            // Footer - center align
            Paragraph footer = new Paragraph(String.format("Bao cao duoc tao vao: %s", 
                    LocalDate.now().format(formatter)))
                    .setFont(font)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(30);
            document.add(footer);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF report", e);
        }
    }    private String formatCurrency(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            return "0 VND";
        }
        DecimalFormat formatter = new DecimalFormat("#,###.00");
        String formatted = formatter.format(amount);
        // Remove trailing .00 if it's a whole number
        if (formatted.endsWith(".00")) {
            formatted = formatted.substring(0, formatted.length() - 3);
        }
        return formatted + " VND";
    }private Cell createHeaderCell(String content) {
        try {
            return new Cell()
                .add(new Paragraph(removeDiacritics(content)))
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER);
        } catch (Exception e) {
            return new Cell()
                .add(new Paragraph(removeDiacritics(content)))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER);
        }
    }

    private Cell createDataCell(String content) {
        try {
            return new Cell()
                .add(new Paragraph(content))
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
                .setTextAlignment(TextAlignment.CENTER);
        } catch (Exception e) {
            return new Cell()
                .add(new Paragraph(content))
                .setTextAlignment(TextAlignment.CENTER);
        }
    }

    private String removeDiacritics(String text) {
        return removeVietnameseDiacritics(text);
    }

    // Hàm chuyển đổi tiếng Việt có dấu thành không dấu
    private String removeVietnameseDiacritics(String text) {
        if (text == null) return "";
        
        String[] vietnameseChars = {
            "àáạảãâầấậẩẫăằắặẳẵ", "a",
            "èéẹẻẽêềếệểễ", "e", 
            "ìíịỉĩ", "i",
            "òóọỏõôồốộổỗơờớợởỡ", "o",
            "ùúụủũưừứựửữ", "u",
            "ỳýỵỷỹ", "y",
            "đ", "d",
            "ÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴ", "A",
            "ÈÉẸẺẼÊỀẾỆỂỄ", "E",
            "ÌÍỊỈĨ", "I", 
            "ÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỬ", "O",
            "ÙÚỤỦŨƯỪỨỰỬỮ", "U",
            "ỲÝỴỶỸ", "Y",
            "Đ", "D"
        };
        
        String result = text;
        for (int i = 0; i < vietnameseChars.length; i += 2) {
            String vietnameseGroup = vietnameseChars[i];
            String replacement = vietnameseChars[i + 1];
            for (char c : vietnameseGroup.toCharArray()) {
                result = result.replace(c, replacement.charAt(0));
            }
        }
        return result;
    }

    /**
     * Generate BM5.1 - Báo cáo doanh số hoạt động ngày
     */
    public byte[] generateDailyReport(List<DailyReportDTO> reportData, LocalDate reportDate) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
              // Title
            Paragraph title = new Paragraph("BM5.1 - Bao Cao Doanh So Hoat Dong Ngay")
                    .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                    .setFontSize(16)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10);
            document.add(title);
            
            // Date
            Paragraph dateInfo = new Paragraph("Ngay: " + reportDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(dateInfo);
            
            // Table
            Table table = new Table(UnitValue.createPercentArray(new float[]{1, 4, 2, 2, 2}));
            table.setWidth(UnitValue.createPercentValue(100));
              // Headers
            table.addHeaderCell(createHeaderCell("STT"));
            table.addHeaderCell(createHeaderCell("Loai Tiet Kiem"));
            table.addHeaderCell(createHeaderCell("Tong Thu"));
            table.addHeaderCell(createHeaderCell("Tong Chi"));
            table.addHeaderCell(createHeaderCell("Chenh Lech"));
            
            // Data rows
            for (DailyReportDTO item : reportData) {
                table.addCell(createDataCell(item.getStt().toString()));
                table.addCell(createDataCell(removeDiacritics(item.getLoaiTietKiem())));
                table.addCell(createDataCell(formatCurrency(item.getTongThu())));
                table.addCell(createDataCell(formatCurrency(item.getTongChi())));
                table.addCell(createDataCell(formatCurrency(item.getChenhLech())));
            }
            
            document.add(table);
            document.close();
            
            return baos.toByteArray();
        } catch (Exception e) {
            logger.error("Error generating daily report PDF", e);
            throw new RuntimeException("Error generating daily report PDF", e);
        }
    }

    /**
     * Generate BM5.2 - Báo cáo mở/đóng sổ tháng
     */
    public byte[] generateMonthlyReport(List<MonthlyReportDTO> reportData, LocalDate fromDate, LocalDate toDate) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
              // Title
            Paragraph title = new Paragraph("BM5.2 - Bao Cao Mo/Dong So Thang")
                    .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                    .setFontSize(16)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10);
            document.add(title);
            
            // Date range
            Paragraph dateInfo = new Paragraph("Tu ngay: " + fromDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                                             " den ngay: " + toDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(dateInfo);
            
            // Table
            Table table = new Table(UnitValue.createPercentArray(new float[]{1, 3, 2, 2, 2}));
            table.setWidth(UnitValue.createPercentValue(100));
            
            // Headers
            table.addHeaderCell(createHeaderCell("STT"));
            table.addHeaderCell(createHeaderCell("Ngay"));
            table.addHeaderCell(createHeaderCell("So So Mo"));
            table.addHeaderCell(createHeaderCell("So So Dong"));
            table.addHeaderCell(createHeaderCell("Chenh Lech"));
            
            // Data rows
            for (MonthlyReportDTO item : reportData) {
                table.addCell(createDataCell(item.getStt().toString()));
                table.addCell(createDataCell(item.getNgay()));
                table.addCell(createDataCell(item.getSoSoMo().toString()));
                table.addCell(createDataCell(item.getSoSoDong().toString()));
                table.addCell(createDataCell(item.getChenhLech().toString())); // Số lượng, không phải tiền tệ
            }
            
            document.add(table);
            document.close();
            
            return baos.toByteArray();
        } catch (Exception e) {
            logger.error("Error generating monthly report PDF", e);
            throw new RuntimeException("Error generating monthly report PDF", e);
        }
    }
}
