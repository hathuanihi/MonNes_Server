package com.SE104.quan_ly_so_tiet_kiem.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PhieuGuiTienRequest {
    @NotNull(message = "Mã số tiết kiệm không được trống")
    private Integer maSoTietKiem;

    @NotNull(message = "Số tiền gửi không được trống")
    @DecimalMin(value = "100000", message = "Số tiền gửi thêm tối thiểu là 100,000 VND")
    private BigDecimal soTienGui;
}
