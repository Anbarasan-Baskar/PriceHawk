package com.pricehawk.backend.payload.response;

import com.pricehawk.backend.entity.Watchlist;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WatchlistResponse {
    private Long id;
    private ProductResponse product;
    private BigDecimal targetPrice;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public static WatchlistResponse from(Watchlist watchlist) {
        WatchlistResponse response = new WatchlistResponse();
        response.setId(watchlist.getId());
        response.setProduct(ProductResponse.from(watchlist.getProduct()));
        response.setTargetPrice(watchlist.getTargetPrice());
        response.setIsActive(watchlist.getIsActive());
        response.setCreatedAt(watchlist.getCreatedAt());
        return response;
    }
}
