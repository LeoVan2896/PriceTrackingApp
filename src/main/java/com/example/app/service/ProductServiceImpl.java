package com.example.app.service;

import com.example.app.dto.PriceHistoryRequestDto;
import com.example.app.dto.PriceHistoryResponseDto;
import com.example.app.dto.ProductRequestDto;
import com.example.app.dto.ProductResponseDto;
import com.example.app.exception.ProductNotFoundException;
import com.example.app.model.PriceHistory;
import com.example.app.model.Product;
import com.example.app.repository.PriceHistoryRepository;
import com.example.app.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    @Override
    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto request) {
        log.info("Creating product: {}", request.getName());
        Product product = Product.builder()
                .name(request.getName())
                .brand(request.getBrand())
                .category(request.getCategory())
                .url(request.getUrl())
                .autoTrack(request.isAutoTrack())
                .build();
        Product saved = productRepository.save(product);
        return toProductResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::toProductResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return toProductResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getProductsByCategory(String category) {
        return productRepository.findByCategory(category)
                .stream()
                .map(this::toProductResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
        log.info("Deleted product with ID: {}", id);
    }

    @Override
    @Transactional
    public PriceHistoryResponseDto addPrice(PriceHistoryRequestDto request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));

        PriceHistory priceHistory = PriceHistory.builder()
                .product(product)
                .storeName(request.getStoreName())
                .price(request.getPrice())
                .build();

        PriceHistory saved = priceHistoryRepository.save(priceHistory);
        log.info("Recorded price {} for product ID: {}", request.getPrice(), request.getProductId());
        return toPriceHistoryResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PriceHistoryResponseDto> getPriceHistory(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }
        return priceHistoryRepository.findByProductIdOrderByRecordedAtDesc(productId)
                .stream()
                .map(this::toPriceHistoryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PriceHistoryResponseDto getLowestPrice(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }
        return priceHistoryRepository.findLowestPriceForProduct(productId)
                .map(this::toPriceHistoryResponse)
                .orElseThrow(() -> new RuntimeException("No prices recorded for product ID: " + productId));
    }

    // --- Private mapping methods ---

    private ProductResponseDto toProductResponse(Product product) {
        return ProductResponseDto.builder()
                .id(product.getId())
                .name(product.getName())
                .brand(product.getBrand())
                .category(product.getCategory())
                .createdAt(product.getCreatedAt())
                .url(product.getUrl())
                .autoTrack(product.isAutoTrack())
                .build();
    }

    private PriceHistoryResponseDto toPriceHistoryResponse(PriceHistory ph) {
        return PriceHistoryResponseDto.builder()
                .id(ph.getId())
                .productId(ph.getProduct().getId())
                .productName(ph.getProduct().getName())
                .storeName(ph.getStoreName())
                .price(ph.getPrice())
                .recordedAt(ph.getRecordedAt())
                .build();
    }
}