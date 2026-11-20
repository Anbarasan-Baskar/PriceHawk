package com.pricehawk.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class CompareService {

    private final RestTemplate restTemplate;
    private final ProductSelectionService selectionService;
    private final String aiServiceUrl;

    public CompareService(RestTemplate restTemplate,
            ProductSelectionService selectionService,
            @Value("${ai.service.url:http://localhost:8000}") String aiServiceUrl) {
        this.restTemplate = restTemplate;
        this.selectionService = selectionService;
        this.aiServiceUrl = aiServiceUrl;
    }

    public Map<String, Object> compareByTitle(String title) {
        return compareByTitle(title, null, null);
    }

    public Map<String, Object> compareByTitle(String title, Long productId) {
        return compareByTitle(title, productId, null);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> compareByTitle(String title, Long productId, String currentPlatformId) {
        if (title == null || title.isBlank()) {
            return Map.of("error", "title is required", "platforms", Map.of());
        }

        String url = aiServiceUrl + "/instant-compare";
        Map<String, Object> req = new HashMap<>();
        req.put("title", title.trim());
        if (productId != null) {
            req.put("productId", productId);
        }

        try {
            Map<String, Object> aiResp = restTemplate.postForObject(url, req, Map.class);
            return selectionService.selectBestProducts(aiResp, currentPlatformId);
        } catch (RestClientException e) {
            return Map.of(
                    "error", "AI service request failed: " + e.getMessage(),
                    "platforms", Map.of());
        }
    }
}