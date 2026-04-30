package com.example.app.service;

import com.example.app.dto.PriceHistoryRequestDto;
import com.example.app.dto.PriceHistoryResponseDto;
import com.example.app.dto.ProductRequestDto;
import com.example.app.dto.ProductResponseDto;

import java.util.List;

public interface ProductService {
    ProductResponseDto createProduct(ProductRequestDto request);
    List<ProductResponseDto> getAllProducts();
    ProductResponseDto getProductById(Long id);
    List<ProductResponseDto> getProductsByCategory(String category);
    void deleteProduct(Long id);
    PriceHistoryResponseDto addPrice(PriceHistoryRequestDto request);
    List<PriceHistoryResponseDto> getPriceHistory(Long productId);
    PriceHistoryResponseDto getLowestPrice(Long productId);
}