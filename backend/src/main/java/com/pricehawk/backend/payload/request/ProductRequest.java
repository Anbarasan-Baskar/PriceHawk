package com.pricehawk.backend.payload.request;

import com.pricehawk.backend.entity.Product;
import lombok.Data;

@Data
public class ProductRequest {
    private String platformId;
    private String platform;
    private String groupId;
    private String name;
    private String imageUrl;
    private String productUrl;
    private Integer currentPrice;
    private Double rating;
    private Integer reviewCount;
    private Boolean isTracked;

    public Product toEntity() {
        Product product = new Product();
        product.setPlatformId(platformId);
        product.setPlatform(platform);
        product.setGroupId(groupId);
        product.setName(name);
        product.setImageUrl(imageUrl);
        product.setProductUrl(productUrl);
        product.setCurrentPrice(currentPrice);
        product.setRating(rating);
        product.setReviewCount(reviewCount);
        product.setIsTracked(isTracked != null ? isTracked : false);
        return product;
    }
}
