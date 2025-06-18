package com.SE104.quan_ly_so_tiet_kiem.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.charset.StandardCharsets;

/**
 * Global Exception Handler để xử lý các lỗi trong toàn bộ ứng dụng
 * Đặc biệt tập trung vào việc xử lý encoding UTF-8 cho tiếng Việt
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Xử lý IllegalArgumentException (lỗi nghiệp vụ chính)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        String message = ensureUtf8Encoding(ex.getMessage());
        logger.error("❌ Lỗi nghiệp vụ: {}", message);
        
        ErrorResponse error = new ErrorResponse(
            "BUSINESS_ERROR",
            message,
            HttpStatus.BAD_REQUEST.value()
        );
        
        return ResponseEntity.badRequest().body(error);
    }
    
    /**
     * Xử lý RuntimeException tổng quát
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        String message = ensureUtf8Encoding(ex.getMessage());
        logger.error("❌ Lỗi runtime: {}", message, ex);
        
        ErrorResponse error = new ErrorResponse(
            "RUNTIME_ERROR",
            message,
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    /**
     * Xử lý Exception tổng quát
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        String message = ensureUtf8Encoding(ex.getMessage());
        logger.error("❌ Lỗi không xác định: {}", message, ex);
        
        ErrorResponse error = new ErrorResponse(
            "INTERNAL_ERROR",
            "Đã xảy ra lỗi không xác định. Vui lòng thử lại sau.",
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    /**
     * Đảm bảo chuỗi được encode đúng UTF-8
     */
    private String ensureUtf8Encoding(String input) {
        if (input == null) {
            return "Lỗi không xác định";
        }
        
        try {
            // Kiểm tra xem chuỗi có bị lỗi encoding không
            if (input.contains("Γ") || input.contains("ß") || input.contains("├") || input.contains("╗")) {
                logger.warn("⚠️ Phát hiện lỗi encoding: {}", input);
                // Thử decode lại
                byte[] bytes = input.getBytes(StandardCharsets.ISO_8859_1);
                return new String(bytes, StandardCharsets.UTF_8);
            }
            
            // Đảm bảo chuỗi được encode đúng UTF-8
            byte[] utf8Bytes = input.getBytes(StandardCharsets.UTF_8);
            return new String(utf8Bytes, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            logger.error("❌ Lỗi xử lý encoding: {}", e.getMessage());
            return input; // Trả về chuỗi gốc nếu không xử lý được
        }
    }
    
    /**
     * Error Response DTO
     */
    public static class ErrorResponse {
        private String errorCode;
        private String message;
        private int status;
        private long timestamp;
        
        public ErrorResponse(String errorCode, String message, int status) {
            this.errorCode = errorCode;
            this.message = message;
            this.status = status;
            this.timestamp = System.currentTimeMillis();
        }
        
        // Getters
        public String getErrorCode() { return errorCode; }
        public String getMessage() { return message; }
        public int getStatus() { return status; }
        public long getTimestamp() { return timestamp; }
        
        // Setters
        public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
        public void setMessage(String message) { this.message = message; }
        public void setStatus(int status) { this.status = status; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}
