package com.example.app.repository;

import com.example.app.model.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    List<PriceHistory> findByProductIdOrderByRecordedAtDesc(Long productId);

    @Query("SELECT p FROM PriceHistory p WHERE p.product.id = :productId ORDER BY p.price ASC LIMIT 1")
    Optional<PriceHistory> findLowestPriceForProduct(@Param("productId") Long productId);


}