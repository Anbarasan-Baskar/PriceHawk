package com.pricehawk.backend.payload.response;

import java.util.Map;

public class CompareResponse {

    private String best;
    private double difference;
    private Map<String, Double> prices;

    public CompareResponse(String best, double difference, Map<String, Double> prices) {
        this.best = best;
        this.difference = difference;
        this.prices = prices;
    }

    public String getBest() {
        return best;
    }

    public double getDifference() {
        return difference;
    }

    public Map<String, Double> getPrices() {
        return prices;
    }
}
