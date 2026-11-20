package com.pricehawk.backend.payload.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WatchlistRequest {
    private Long productId;
    private BigDecimal targetPrice;
}
