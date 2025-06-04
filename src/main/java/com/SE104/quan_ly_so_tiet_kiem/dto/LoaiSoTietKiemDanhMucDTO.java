package com.SE104.quan_ly_so_tiet_kiem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoaiSoTietKiemDanhMucDTO {
    private Integer maLoaiSoTietKiem;
    private String tenLoaiSoTietKiem;

    public Integer getMaLoaiSoTietKiem() {
        return maLoaiSoTietKiem;
    }

    public void setMaLoaiSoTietKiem(Integer maLoaiSoTietKiem) {
        this.maLoaiSoTietKiem = maLoaiSoTietKiem;
    }

    public String getTenLoaiSoTietKiem() {
        return tenLoaiSoTietKiem;
    }

    public void setTenLoaiSoTietKiem(String tenLoaiSoTietKiem) {
        this.tenLoaiSoTietKiem = tenLoaiSoTietKiem;
    }
}
