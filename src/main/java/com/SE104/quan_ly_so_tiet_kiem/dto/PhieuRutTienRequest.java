package com.SE104.quan_ly_so_tiet_kiem.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PhieuRutTienRequest {
    @NotNull(message = "Mã số tiết kiệm không được trống")
    private Integer maSoTietKiem;

    @NotNull(message = "Số tiền rút không được trống")
    @DecimalMin(value = "1", message = "Số tiền rút phải lớn hơn 0")
    private BigDecimal soTienRut;
}
