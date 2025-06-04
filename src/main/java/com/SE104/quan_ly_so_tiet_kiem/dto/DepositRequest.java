package com.SE104.quan_ly_so_tiet_kiem.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DepositRequest { 

    @NotNull(message = "Số tiền gửi không được trống")
    @DecimalMin(value = "10000", message = "Số tiền gửi phải lớn hơn 0 và tối thiểu theo quy định.") 
    private BigDecimal soTien;
}