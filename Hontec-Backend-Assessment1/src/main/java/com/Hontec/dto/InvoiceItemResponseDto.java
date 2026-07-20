package com.Hontec.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceItemResponseDto {
    private String productName;
    private String batchNo;
    private Integer qty;
    private BigDecimal unitPrice;
    private BigDecimal gstRate;
    private BigDecimal itemTotal; // Calculated as qty * unitPrice * (1 + gstRate/100)
}
