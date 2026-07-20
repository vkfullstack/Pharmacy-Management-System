package com.Hontec.service;

import com.Hontec.dto.InvoiceItemResponseDto;
import com.Hontec.dto.InvoiceResponseDto;
import com.Hontec.exception.BatchExpiredException;
import com.Hontec.exception.InsufficientStockException;
import com.Hontec.exception.ResourceNotFoundException;
import com.Hontec.model.Batch;
import com.Hontec.model.Invoice;
import com.Hontec.model.InvoiceItem;
import com.Hontec.model.InvoiceStatus;
import com.Hontec.model.Product;
import com.Hontec.repository.BatchRepository;
import com.Hontec.repository.InvoiceRepository;
import com.Hontec.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;
    private final BatchRepository batchRepository;
    private final SendGridEmailService sendGridEmailService;

    public InvoiceResponseDto getInvoiceById(Long invoiceId) {
        Invoice invoice = invoiceRepository.findByIdWithItems(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));

        return mapToResponseDto(invoice);
    }

    @Transactional
    public InvoiceResponseDto dispenseInvoice(Long invoiceId) {
        log.info("Starting dispense operation for Invoice ID: {}", invoiceId);

        Invoice invoice = invoiceRepository.findByIdWithItems(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));

        if (invoice.getStatus() != InvoiceStatus.PENDING) {
            throw new IllegalArgumentException("Invoice is not in PENDING status. Current status: " + invoice.getStatus());
        }

        LocalDate today = LocalDate.now();

        // Step 1: Pre-validation of all invoice items
        for (InvoiceItem item : invoice.getItems()) {
            Batch batch = item.getBatch();
            Product product = item.getProduct();

            log.info("Validating Item - Product: {}, Batch: {}, Req Qty: {}, Batch Qty: {}, Expiry: {}",
                    product.getName(), batch.getBatchNo(), item.getQty(), batch.getQty(), batch.getExpiryDate());

            // Check if batch is expired
            if (batch.getExpiryDate().isBefore(today)) {
                throw new BatchExpiredException(String.format("Batch %s for product '%s' has expired (Expiry: %s).",
                        batch.getBatchNo(), product.getName(), batch.getExpiryDate()));
            }

            // Check if batch has sufficient stock
            if (batch.getQty() < item.getQty()) {
                throw new InsufficientStockException(String.format("Insufficient stock in batch %s for product '%s'. Required: %d, Available: %d.",
                        batch.getBatchNo(), product.getName(), item.getQty(), batch.getQty()));
            }
        }

        // Step 2: Perform inventory deduction
        for (InvoiceItem item : invoice.getItems()) {
            Batch batch = item.getBatch();
            Product product = item.getProduct();

            // Deduct from batch
            batch.setQty(batch.getQty() - item.getQty());
            batchRepository.save(batch);

            // Deduct from overall product stock
            if (product.getStockQty() != null) {
                product.setStockQty(Math.max(0, product.getStockQty() - item.getQty()));
                productRepository.save(product);
            }
        }

        // Step 3: Update invoice status
        invoice.setStatus(InvoiceStatus.DISPENSED);
        Invoice savedInvoice = invoiceRepository.save(invoice);

        log.info("Invoice ID: {} successfully marked as DISPENSED", invoiceId);

        // Step 4: Send SendGrid Email Notification
        sendGridEmailService.sendDispenseNotification(savedInvoice);

        return mapToResponseDto(savedInvoice);
    }

    private InvoiceResponseDto mapToResponseDto(Invoice invoice) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<InvoiceItemResponseDto> itemDtos = new ArrayList<>();

        for (InvoiceItem item : invoice.getItems()) {
            BigDecimal qty = BigDecimal.valueOf(item.getQty());
            BigDecimal rawTotal = qty.multiply(item.getUnitPrice());
            // gstRate is parsed, e.g. 18.00 means 18%
            BigDecimal gstPercentage = item.getGstRate().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            BigDecimal taxMultiplier = BigDecimal.ONE.add(gstPercentage);
            BigDecimal itemTotal = rawTotal.multiply(taxMultiplier).setScale(2, RoundingMode.HALF_UP);

            totalAmount = totalAmount.add(itemTotal);

            itemDtos.add(InvoiceItemResponseDto.builder()
                    .productName(item.getProduct().getName())
                    .batchNo(item.getBatch().getBatchNo())
                    .qty(item.getQty())
                    .unitPrice(item.getUnitPrice())
                    .gstRate(item.getGstRate())
                    .itemTotal(itemTotal)
                    .build());
        }

        String doctorName = invoice.getDoctor() != null ? invoice.getDoctor().getName() : "Self-prescribed / None";

        return InvoiceResponseDto.builder()
                .id(invoice.getId())
                .customerName(invoice.getCustomer().getName())
                .doctorName(doctorName)
                .items(itemDtos)
                .totalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP))
                .status(invoice.getStatus().name())
                .build();
    }
}
