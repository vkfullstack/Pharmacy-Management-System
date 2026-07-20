package com.Hontec.repository;

import com.Hontec.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    @Query("SELECT i FROM Invoice i LEFT JOIN FETCH i.items WHERE i.id = :id")
    Optional<Invoice> findByIdWithItems(@Param("id") Long id);
}
