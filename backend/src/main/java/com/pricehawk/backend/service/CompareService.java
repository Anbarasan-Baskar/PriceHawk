package com.pricehawk.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class CompareService {

    private final RestTemplate restTemplate;
    private final String aiServiceUrl;

    public CompareService(RestTemplate restTemplate,
                          @Value("${ai.service.url:http://localhost:8000}") String aiServiceUrl) {
        this.restTemplate = restTemplate;
        this.aiServiceUrl = aiServiceUrl;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> compareByTitle(String title) {
        String url = aiServiceUrl + "/instant-compare";
        Map<String, String> req = Map.of("title", title);

        Map<String, Object> aiResp = restTemplate.postForObject(url, req, Map.class);
        if (aiResp == null || aiResp.isEmpty()) {
            return Map.of();
        }

        Object platformsObj = aiResp.get("platforms");
        if (!(platformsObj instanceof Map)) {
            // Unexpected shape from AI service; return as-is
            return aiResp;
        }

        Map<String, Object> platforms = (Map<String, Object>) platformsObj;
        Map<String, Object> finalPlatforms = new HashMap<>();

        double minConfidence = 0.35;
        int maxResultsPerPlatform = 3; // business rule: choose up to 3 best matches per platform

        for (Map.Entry<String, Object> entry : platforms.entrySet()) {
            String platform = entry.getKey();
            Object listObj = entry.getValue();

            if (!(listObj instanceof List)) {
                finalPlatforms.put(platform, Map.of("best", List.of(), "lowest", null));
                continue;
            }

            List<Map<String, Object>> items = (List<Map<String, Object>>) listObj;
            List<Map<String, Object>> valid = new ArrayList<>();

            // filter & normalize
            for (Map<String, Object> item : items) {
                Object priceObj = item.get("price");
                Double price = null;
                if (priceObj instanceof Number) {
                    price = ((Number) priceObj).doubleValue();
                } else if (priceObj instanceof String) {
                    try {
                        price = Double.parseDouble(((String) priceObj).replaceAll(",", ""));
                    } catch (Exception ignored) {
                    }
                }

                Object confObj = item.get("confidence");
                Double confidence = null;
                if (confObj instanceof Number) {
                    confidence = ((Number) confObj).doubleValue();
                } else if (confObj instanceof String) {
                    try {
                        confidence = Double.parseDouble((String) confObj);
                    } catch (Exception ignored) {
                    }
                }

                if (price != null && confidence != null && confidence >= minConfidence) {
                    item.put("price", price);
                    item.put("confidence", confidence);
                    valid.add(item);
                }
            }

            if (valid.isEmpty()) {
                finalPlatforms.put(platform, Map.of("best", List.of(), "lowest", null));
                continue;
            }

            // determine lowest
            Map<String, Object> lowest = Collections.min(valid, Comparator.comparingDouble(i -> ((Number) i.get("price")).doubleValue()));

            // sort by confidence desc then price asc
            valid.sort((a, b) -> {
                double ca = ((Number) a.get("confidence")).doubleValue();
                double cb = ((Number) b.get("confidence")).doubleValue();
                int c = Double.compare(cb, ca);
                if (c != 0) return c;
                double pa = ((Number) a.get("price")).doubleValue();
                double pb = ((Number) b.get("price")).doubleValue();
                return Double.compare(pa, pb);
            });

            List<Map<String, Object>> topN = new ArrayList<>();
            for (Map<String, Object> it : valid) {
                if (topN.size() >= maxResultsPerPlatform) break;
                topN.add(it);
            }

            if (!topN.contains(lowest)) {
                if (topN.size() < maxResultsPerPlatform) topN.add(lowest);
                else topN.set(topN.size() - 1, lowest);
            }

            // mark is_lowest
            for (Map<String, Object> it : topN) {
                it.put("is_lowest", it == lowest);
            }

            finalPlatforms.put(platform, Map.of("best", topN, "lowest", lowest));
        }

        return Map.of("platforms", finalPlatforms);
    }
}
