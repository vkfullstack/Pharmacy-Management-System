package com.Hontec.service;

import com.Hontec.dto.ProductResponseDto;
import com.Hontec.model.Product;
import com.Hontec.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product productLowStock;
    private Product productInStock;

    @BeforeEach
    void setUp() {
        productLowStock = Product.builder()
                .id(1L)
                .name("Ibuprofen")
                .category("analgesic")
                .stockQty(5)
                .lowStockThreshold(10)
                .expiryDate(LocalDate.now().plusMonths(6))
                .build();

        productInStock = Product.builder()
                .id(2L)
                .name("Amoxicillin")
                .category("antibiotic")
                .stockQty(100)
                .lowStockThreshold(10)
                .expiryDate(LocalDate.now().plusMonths(12))
                .build();
    }

    @Test
    void getProducts_FilteredAndMapped() {
        List<Product> products = List.of(productLowStock, productInStock);
        Page<Product> productPage = new PageImpl<>(products, PageRequest.of(0, 2), 2);

        when(productRepository.findProductsFiltered(
                eq("antibiotic"), eq(false), eq(false), any(LocalDate.class), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(productPage);

        Page<ProductResponseDto> result = productService.getProducts("antibiotic", false, false, 0, 2);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());

        ProductResponseDto dto1 = result.getContent().get(0);
        assertEquals("Ibuprofen", dto1.getName());
        assertEquals("analgesic", dto1.getCategory());
        assertEquals(5, dto1.getStockQty());
        assertTrue(dto1.getLowStock()); // 5 <= 10 -> Low stock is true

        ProductResponseDto dto2 = result.getContent().get(1);
        assertEquals("Amoxicillin", dto2.getName());
        assertEquals("antibiotic", dto2.getCategory());
        assertEquals(100, dto2.getStockQty());
        assertFalse(dto2.getLowStock()); // 100 <= 10 -> Low stock is false
    }
}
