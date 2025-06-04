package com.SE104.quan_ly_so_tiet_kiem.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class WithdrawRequest { 

    @NotNull(message = "Số tiền rút không được trống")
    @DecimalMin(value = "1000", message = "Số tiền rút phải lớn hơn 0 và tuân theo quy định.") 
    private BigDecimal soTien;
}