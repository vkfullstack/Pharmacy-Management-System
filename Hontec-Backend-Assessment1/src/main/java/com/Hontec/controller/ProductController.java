package com.Hontec.controller;

import com.Hontec.dto.ErrorResponse;
import com.Hontec.dto.ProductResponseDto;
import com.Hontec.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Endpoints for retrieving products with filters and pagination")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "List all products with filters and pagination", description = "Returns a paginated list of products. Filterable by category, stock level (low stock relative to threshold), and batch expiration (expiring within 30 days). Requires authentication.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved products page")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Page<ProductResponseDto>> getProducts(
            @Parameter(description = "Category to filter by (e.g. antibiotic)") 
            @RequestParam(required = false) String category,
            
            @Parameter(description = "If true, returns products whose stock_qty is less than or equal to low_stock_threshold") 
            @RequestParam(required = false) Boolean lowStock,
            
            @Parameter(description = "If true, returns products that have a batch expiring within 30 days") 
            @RequestParam(required = false) Boolean expiringSoon,
            
            @Parameter(description = "Zero-based page index") 
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Size of the page to retrieve") 
            @RequestParam(defaultValue = "20") int size) {
        
        Page<ProductResponseDto> products = productService.getProducts(category, lowStock, expiringSoon, page, size);
        return ResponseEntity.ok(products);
    }
}
