package com.pricehawk.backend.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PredictionResponse {
    @JsonProperty("predicted_price")
    private Double predictedPrice;

    @JsonProperty("confidence_score")
    private Double confidenceScore;

    private String trend;
    private String confidence;
    private Double price;
    private String error;

    public static PredictionResponse fallback(double currentPrice) {
        PredictionResponse response = new PredictionResponse();
        response.setPredictedPrice(currentPrice);
        response.setConfidenceScore(0.0);
        response.setTrend("UNKNOWN");
        response.setConfidence("Low");
        response.setPrice(currentPrice);
        return response;
    }

    public static PredictionResponse error(String message) {
        PredictionResponse response = new PredictionResponse();
        response.setError(message);
        return response;
    }
}
