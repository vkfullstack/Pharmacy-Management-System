package com.Hontec.controller;

import com.Hontec.dto.ErrorResponse;
import com.Hontec.dto.InvoiceResponseDto;
import com.Hontec.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoices", description = "Endpoints for retrieving invoices and dispensing medications")
@SecurityRequirement(name = "bearerAuth")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping("/{invoiceId}")
    @Operation(summary = "Get invoice by ID", description = "Fetches a single invoice details by ID, including customer name, doctor name, lines items, and total amount. Requires authentication.")
    @ApiResponse(responseCode = "200", description = "Invoice details retrieved successfully",
            content = @Content(schema = @Schema(implementation = InvoiceResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Invoice not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<InvoiceResponseDto> getInvoiceById(
            @Parameter(description = "ID of the invoice to retrieve") 
            @PathVariable Long invoiceId) {
        InvoiceResponseDto response = invoiceService.getInvoiceById(invoiceId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{invoiceId}/dispense")
    @Operation(summary = "Dispense an invoice", description = "Marks a PENDING invoice as DISPENSED. Deducts batch stock quantities and syncs total product quantities. Rejects (400 Bad Request) if any batch has insufficient stock or is expired. Sends email notification to manager on success. Requires role: PHARMACIST.")
    @ApiResponse(responseCode = "200", description = "Invoice dispensed successfully",
            content = @Content(schema = @Schema(implementation = InvoiceResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Bad Request - Insufficient stock, expired batch, or invoice already dispensed",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden - Requires role PHARMACIST",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Invoice not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<InvoiceResponseDto> dispenseInvoice(
            @Parameter(description = "ID of the invoice to dispense") 
            @PathVariable Long invoiceId) {
        InvoiceResponseDto response = invoiceService.dispenseInvoice(invoiceId);
        return ResponseEntity.ok(response);
    }
}
