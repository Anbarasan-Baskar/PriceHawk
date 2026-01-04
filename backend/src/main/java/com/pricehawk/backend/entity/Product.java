package com.pricehawk.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"platform_id", "platform"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "platform_id", nullable = false)
    private String platformId; // ASIN or FSN

    @Column(nullable = false)
    private String platform; // "AMAZON" or "FLIPKART"

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "product_url", columnDefinition = "TEXT", nullable = false)
    private String productUrl;

    @Column(name = "current_price")
    private Integer currentPrice;

    private Double rating;

    @Column(name = "review_count")
    private Integer reviewCount;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "is_tracked")
    private Boolean isTracked = false;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}
