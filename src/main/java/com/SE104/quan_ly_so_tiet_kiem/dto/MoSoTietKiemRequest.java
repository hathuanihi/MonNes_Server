package com.SE104.quan_ly_so_tiet_kiem.dto;

import java.math.BigDecimal;
// import java.time.LocalDate; 

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MoSoTietKiemRequest { 

    @NotNull(message = "Mã sản phẩm sổ tiết kiệm không được trống")
    private Integer soTietKiemSanPhamId; 


    @NotBlank(message = "Tên sổ mở không được để trống")
    @Size(max = 100, message = "Tên sổ mở không được vượt quá 100 ký tự")
    private String tenSoMo;

    @NotNull(message = "Số tiền gửi ban đầu không được trống")
    @DecimalMin(value = "100000", message = "Số tiền gửi không hợp lệ (tối thiểu dựa trên loại sổ đã chọn)") 
    private BigDecimal soTienGuiBanDau;
}