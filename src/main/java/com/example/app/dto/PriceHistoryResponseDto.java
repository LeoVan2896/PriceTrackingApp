package com.example.app.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceHistoryResponseDto {
    private Long id;
    private Long productId;
    private String productName;
    private String storeName;
    private BigDecimal price;
    private LocalDateTime recordedAt;
}