package com.pricehawk.backend.service;

import com.pricehawk.backend.payload.request.PredictionRequest;
import com.pricehawk.backend.payload.request.PriceHistoryPoint;
import com.pricehawk.backend.payload.response.PredictionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class AIClientService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    public PredictionResponse getPrediction(double currentPrice, List<PriceHistoryPoint> history) {
        String url = aiServiceUrl + "/predict/price";

        PredictionRequest request = new PredictionRequest();
        request.setHistory(history);

        try {
            PredictionResponse response = restTemplate.postForObject(url, request, PredictionResponse.class);
            return response != null ? response : PredictionResponse.fallback(currentPrice);
        } catch (Exception e) {
            e.printStackTrace();
            return PredictionResponse.fallback(currentPrice);
        }
    }
}
