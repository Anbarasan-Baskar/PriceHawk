package com.pricehawk.backend.payload.response;

import com.pricehawk.backend.entity.Product;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductResponse {
    private Long id;
    private String platformId;
    private String platform;
    private String groupId;
    private String name;
    private String imageUrl;
    private String productUrl;
    private Integer currentPrice;
    private Double rating;
    private Integer reviewCount;
    private LocalDateTime lastUpdated;
    private Boolean isTracked;

    public static ProductResponse from(Product product) {
        if (product == null) {
            return null;
        }

        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setPlatformId(product.getPlatformId());
        response.setPlatform(product.getPlatform());
        response.setGroupId(product.getGroupId());
        response.setName(product.getName());
        response.setImageUrl(product.getImageUrl());
        response.setProductUrl(product.getProductUrl());
        response.setCurrentPrice(product.getCurrentPrice());
        response.setRating(product.getRating());
        response.setReviewCount(product.getReviewCount());
        response.setLastUpdated(product.getLastUpdated());
        response.setIsTracked(product.getIsTracked());
        return response;
    }
}
