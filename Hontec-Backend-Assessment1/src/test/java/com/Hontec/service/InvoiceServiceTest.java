package com.Hontec.service;

import com.Hontec.dto.InvoiceResponseDto;
import com.Hontec.exception.BatchExpiredException;
import com.Hontec.exception.InsufficientStockException;
import com.Hontec.exception.ResourceNotFoundException;
import com.Hontec.model.Batch;
import com.Hontec.model.Customer;
import com.Hontec.model.Doctor;
import com.Hontec.model.Invoice;
import com.Hontec.model.InvoiceItem;
import com.Hontec.model.InvoiceStatus;
import com.Hontec.model.Product;
import com.Hontec.repository.BatchRepository;
import com.Hontec.repository.InvoiceRepository;
import com.Hontec.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BatchRepository batchRepository;

    @Mock
    private SendGridEmailService sendGridEmailService;

    @InjectMocks
    private InvoiceService invoiceService;

    private Customer customer;
    private Doctor doctor;
    private Product product;
    private Batch batch;
    private Invoice invoice;
    private InvoiceItem item;

    @BeforeEach
    void setUp() {
        customer = Customer.builder().id(1L).name("John Doe").phone("1234567890").build();
        doctor = Doctor.builder().id(1L).name("Dr. Gregory House").specialization("Diagnostics").build();
        
        product = Product.builder()
                .id(1L)
                .name("Amoxicillin 500mg")
                .category("antibiotic")
                .stockQty(100)
                .lowStockThreshold(20)
                .expiryDate(LocalDate.now().plusYears(1))
                .build();

        batch = Batch.builder()
                .id(1L)
                .product(product)
                .batchNo("AMX001")
                .qty(50)
                .expiryDate(LocalDate.now().plusYears(1))
                .mrp(BigDecimal.valueOf(10.0))
                .build();

        invoice = Invoice.builder()
                .id(1L)
                .customer(customer)
                .doctor(doctor)
                .status(InvoiceStatus.PENDING)
                .items(new ArrayList<>())
                .build();

        item = InvoiceItem.builder()
                .id(1L)
                .invoice(invoice)
                .product(product)
                .batch(batch)
                .qty(10)
                .unitPrice(BigDecimal.valueOf(10.0))
                .gstRate(BigDecimal.valueOf(18.0))
                .build();

        invoice.getItems().add(item);
    }

    @Test
    void getInvoiceById_Success() {
        when(invoiceRepository.findByIdWithItems(1L)).thenReturn(Optional.of(invoice));

        InvoiceResponseDto response = invoiceService.getInvoiceById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("John Doe", response.getCustomerName());
        assertEquals("Dr. Gregory House", response.getDoctorName());
        assertEquals("PENDING", response.getStatus());
        assertEquals(1, response.getItems().size());
        
        // Item Total: 10 * 10 * 1.18 = 118.00
        assertEquals(new BigDecimal("118.00"), response.getTotalAmount());
    }

    @Test
    void getInvoiceById_NotFound() {
        when(invoiceRepository.findByIdWithItems(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> invoiceService.getInvoiceById(1L));
    }

    @Test
    void dispenseInvoice_Success() {
        when(invoiceRepository.findByIdWithItems(1L)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);

        InvoiceResponseDto response = invoiceService.dispenseInvoice(1L);

        assertNotNull(response);
        assertEquals("DISPENSED", response.getStatus());
        assertEquals(40, batch.getQty()); // 50 - 10
        assertEquals(90, product.getStockQty()); // 100 - 10

        verify(batchRepository, times(1)).save(batch);
        verify(productRepository, times(1)).save(product);
        verify(invoiceRepository, times(1)).save(invoice);
        verify(sendGridEmailService, times(1)).sendDispenseNotification(invoice);
    }

    @Test
    void dispenseInvoice_AlreadyDispensed() {
        invoice.setStatus(InvoiceStatus.DISPENSED);
        when(invoiceRepository.findByIdWithItems(1L)).thenReturn(Optional.of(invoice));

        assertThrows(IllegalArgumentException.class, () -> invoiceService.dispenseInvoice(1L));

        verify(batchRepository, never()).save(any());
        verify(productRepository, never()).save(any());
        verify(sendGridEmailService, never()).sendDispenseNotification(any());
    }

    @Test
    void dispenseInvoice_InsufficientStock() {
        item.setQty(100); // Exceeds batch qty (50)
        when(invoiceRepository.findByIdWithItems(1L)).thenReturn(Optional.of(invoice));

        assertThrows(InsufficientStockException.class, () -> invoiceService.dispenseInvoice(1L));

        verify(batchRepository, never()).save(any());
        verify(productRepository, never()).save(any());
        verify(sendGridEmailService, never()).sendDispenseNotification(any());
    }

    @Test
    void dispenseInvoice_BatchExpired() {
        batch.setExpiryDate(LocalDate.now().minusDays(1)); // Expired yesterday
        when(invoiceRepository.findByIdWithItems(1L)).thenReturn(Optional.of(invoice));

        assertThrows(BatchExpiredException.class, () -> invoiceService.dispenseInvoice(1L));

        verify(batchRepository, never()).save(any());
        verify(productRepository, never()).save(any());
        verify(sendGridEmailService, never()).sendDispenseNotification(any());
    }
}
