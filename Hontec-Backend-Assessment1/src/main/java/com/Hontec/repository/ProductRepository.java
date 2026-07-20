package com.Hontec.repository;

import com.Hontec.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE " +
           "(:category IS NULL OR p.category = :category) AND " +
           "(:lowStock IS NULL OR :lowStock = false OR p.stockQty <= p.lowStockThreshold) AND " +
           "(:expiringSoon IS NULL OR :expiringSoon = false OR (p.expiryDate BETWEEN :today AND :expLimit))")
    Page<Product> findProductsFiltered(
            @Param("category") String category,
            @Param("lowStock") Boolean lowStock,
            @Param("expiringSoon") Boolean expiringSoon,
            @Param("today") LocalDate today,
            @Param("expLimit") LocalDate expLimit,
            Pageable pageable);
}
