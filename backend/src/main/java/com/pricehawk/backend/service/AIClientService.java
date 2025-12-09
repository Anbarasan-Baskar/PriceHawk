package com.pricehawk.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@Service
public class AIClientService {

    @Autowired
    private RestTemplate restTemplate;

    private final String AI_SERVICE_URL = "http://localhost:8000";

    public Map<String, Object> getPrediction(double currentPrice, List<Map<String, Object>> history) {
        String url = AI_SERVICE_URL + "/predict/price";

        Map<String, Object> request = new HashMap<>();
        request.put("history", history); // AI expects list of {price, date}

        try {
            return restTemplate.postForObject(url, request, Map.class);
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback response if AI is down
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("predicted_price", currentPrice);
            fallback.put("confidence_score", 0.0);
            fallback.put("trend", "UNKNOWN");
            return fallback;
        }
    }
}
