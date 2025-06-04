package com.SE104.quan_ly_so_tiet_kiem.dto;
import lombok.Data;

@Data
public class VerifyPasscodeRequest {
    private String email;
    private String passcode;
}
