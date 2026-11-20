package com.pricehawk.backend.payload.request;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PredictionRequest {
    private Double price;
    private List<PriceHistoryPoint> history = new ArrayList<>();
}
