package com.example.app.controller;

import com.example.app.dto.ApiResponse;
import com.example.app.dto.PriceHistoryRequestDto;
import com.example.app.dto.PriceHistoryResponseDto;
import com.example.app.dto.ProductRequestDto;
import com.example.app.dto.ProductResponseDto;
import com.example.app.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProductResponseDto> createProduct(@RequestBody ProductRequestDto request) {
        log.info("POST /api/products - {}", request.getName());
        return ApiResponse.success(productService.createProduct(request));
    }

    @GetMapping
    public ApiResponse<List<ProductResponseDto>> getAllProducts() {
        return ApiResponse.success(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponseDto> getProductById(@PathVariable Long id) {
        return ApiResponse.success(productService.getProductById(id));
    }

    @GetMapping("/category/{category}")
    public ApiResponse<List<ProductResponseDto>> getByCategory(@PathVariable String category) {
        return ApiResponse.success(productService.getProductsByCategory(category));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id) {
        log.info("DELETE /api/products/{}", id);
        productService.deleteProduct(id);
    }

    @PostMapping("/prices")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PriceHistoryResponseDto> addPrice(@RequestBody PriceHistoryRequestDto request) {
        return ApiResponse.success(productService.addPrice(request));
    }

    @GetMapping("/{id}/prices")
    public ApiResponse<List<PriceHistoryResponseDto>> getPriceHistory(@PathVariable Long id) {
        return ApiResponse.success(productService.getPriceHistory(id));
    }

    @GetMapping("/{id}/prices/lowest")
    public ApiResponse<PriceHistoryResponseDto> getLowestPrice(@PathVariable Long id) {
        return ApiResponse.success(productService.getLowestPrice(id));
    }
}