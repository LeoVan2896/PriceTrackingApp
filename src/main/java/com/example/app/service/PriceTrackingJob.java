package com.example.app.service;

import com.example.app.model.PriceHistory;
import com.example.app.model.Product;
import com.example.app.repository.PriceHistoryRepository;
import com.example.app.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PriceTrackingJob {

    private final ProductRepository productRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final PriceScraperService scraperService;

    // Runs every 30 minutes
    @Scheduled(fixedRateString = "${app.tracking.interval-ms:1800000}")
    public void trackPrices() {
        List<Product> trackedProducts = productRepository.findByAutoTrackTrue();
        log.info("Price tracking job started — {} products to check", trackedProducts.size());

        for (Product product : trackedProducts) {
            try {
                Optional<BigDecimal> price = scraperService.scrapePrice(product.getUrl());

                if (price.isPresent()) {
                    PriceHistory record = PriceHistory.builder()
                            .product(product)
                            .storeName(extractStoreName(product.getUrl()))
                            .price(price.get())
                            .build();

                    priceHistoryRepository.save(record);
                    log.info("Recorded price {} for product: {}", price.get(), product.getName());
                } else {
                    log.warn("Could not scrape price for product: {}", product.getName());
                }

            } catch (Exception e) {
                // Never let one product failure crash the whole job
                log.error("Error tracking product {}: {}", product.getName(), e.getMessage());
            }
        }

        log.info("Price tracking job complete");
    }

    private String extractStoreName(String url) {
        if (url.contains("bestbuy.com"))  return "Best Buy";
        if (url.contains("gamestop.com")) return "GameStop";
        if (url.contains("walmart.com"))  return "Walmart";
        return "Unknown Store";
    }
}