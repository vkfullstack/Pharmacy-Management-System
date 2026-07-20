package com.Hontec.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponseDto {
    private Long id;
    private String customerName;
    private String doctorName;
    private List<InvoiceItemResponseDto> items;
    private BigDecimal totalAmount;
    private String status;
}
