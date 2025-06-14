package com.SE104.quan_ly_so_tiet_kiem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionReportDTO {
    private Integer transactionId;
    private LocalDate transactionDate;
    private String transactionType;
    private BigDecimal amount;
    private String description;
    private Integer accountId;
    private String accountName;
    private String customerName;
    private BigDecimal balanceAfter;
}
