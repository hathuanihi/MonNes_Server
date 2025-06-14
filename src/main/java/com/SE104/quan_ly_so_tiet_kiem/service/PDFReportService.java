package com.SE104.quan_ly_so_tiet_kiem.service;

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
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PDFReportService {

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
        return String.format("%,.0f VND", amount);
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
            "ÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠ", "O",
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
}
