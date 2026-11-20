package com.pricehawk.backend.payload.request;

import lombok.Data;

@Data
public class CompareRequest {
    private String title;
    private String name;
    private String platform;
    private String platformId;
    private Double price;
    private Double currentPrice;
    private String imageUrl;
    private String productUrl;
    private String url;
    private Double rating;
    private Integer reviewCount;
}
