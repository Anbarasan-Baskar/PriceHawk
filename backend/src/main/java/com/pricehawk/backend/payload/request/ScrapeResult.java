package com.pricehawk.backend.payload.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ScrapeResult {
    private String platformId;
    private String platform;
    private String name;
    private BigDecimal currentPrice;
    private String imageUrl;
    private String productUrl;
    private Double rating;
    private Integer reviewCount;
    private Boolean isTracked ;
}
