package com.pricehawk.backend.payload.response;

import com.pricehawk.backend.entity.PriceHistory;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PriceHistoryResponse {
    private Long id;
    private Long productId;
    private Integer price;
    private LocalDateTime recordedAt;

    public static PriceHistoryResponse from(PriceHistory history) {
        PriceHistoryResponse response = new PriceHistoryResponse();
        response.setId(history.getId());
        response.setProductId(history.getProduct().getId());
        response.setPrice(history.getPrice());
        response.setRecordedAt(history.getRecordedAt());
        return response;
    }
}
