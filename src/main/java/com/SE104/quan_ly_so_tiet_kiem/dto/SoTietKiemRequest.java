package com.SE104.quan_ly_so_tiet_kiem.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SoTietKiemRequest { 

    @NotBlank(message = "Tên sản phẩm sổ không được trống")
    private String tenSo; 

    @NotNull(message = "Kỳ hạn không được trống (tháng, 0 cho không kỳ hạn)")
    @Min(value = 0, message = "Kỳ hạn phải là số không âm")
    private Integer kyHan; 

    @NotNull(message = "Lãi suất không được trống")
    @DecimalMin(value = "0.01", message = "Lãi suất phải lớn hơn 0")
    private BigDecimal laiSuat; 

    @NotNull(message = "Tiền gửi ban đầu tối thiểu không được trống")
    @Min(value = 100000, message = "Tiền gửi ban đầu tối thiểu phải ít nhất là 100,000 VND") 
    private Long tienGuiBanDauToiThieu;

    @NotNull(message = "Tiền gửi thêm tối thiểu không được trống")
    @Min(value = 100000, message = "Tiền gửi thêm tối thiểu phải ít nhất là 100,000 VND") 
    private Long tienGuiThemToiThieu;

    @Min(value = 1, message = "Số ngày gửi tối thiểu để rút phải ít nhất là 1 ngày")
    private Integer soNgayGuiToiThieuDeRut;

    @NotNull(message = "Mã danh mục loại sổ không được trống")
    private Integer maLoaiDanhMuc; 
}