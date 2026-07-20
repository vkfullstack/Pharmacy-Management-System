package com.Hontec.service;

import com.Hontec.dto.ProductResponseDto;
import com.Hontec.model.Product;
import com.Hontec.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Page<ProductResponseDto> getProducts(String category, Boolean lowStock, Boolean expiringSoon, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        LocalDate today = LocalDate.now();
        LocalDate expLimit = today.plusDays(30);

        Page<Product> productPage = productRepository.findProductsFiltered(
                category, lowStock, expiringSoon, today, expLimit, pageable);

        return productPage.map(product -> ProductResponseDto.builder()
                .name(product.getName())
                .category(product.getCategory())
                .stockQty(product.getStockQty())
                .lowStock(product.getStockQty() <= product.getLowStockThreshold())
                .build());
    }
}
