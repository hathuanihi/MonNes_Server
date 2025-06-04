package com.SE104.quan_ly_so_tiet_kiem.dto;
import lombok.Data;

@Data
public class MessageResponse {
     private String message;

    public MessageResponse() {
    }

    public MessageResponse(String message) {
        this.message = message;
    }
}
