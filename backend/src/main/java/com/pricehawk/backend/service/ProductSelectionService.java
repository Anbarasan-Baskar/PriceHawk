package com.pricehawk.backend.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProductSelectionService {

    private static final int MAX_RESULTS_PER_PLATFORM = 3;
    private static final double HIGH_CONFIDENCE_FLOOR = 0.70;
    private static final double CONFIDENCE_BAND = 0.15;

    public Map<String, Object> selectBestProducts(Map<String, Object> aiResp) {
        return selectBestProducts(aiResp, null);
    }

    public Map<String, Object> selectBestProducts(Map<String, Object> aiResp, String currentPlatformId) {
        if (aiResp == null || aiResp.isEmpty()) {
            return Map.of("error", "AI service returned no response", "platforms", Map.of());
        }

        if (!(aiResp.get("platforms") instanceof Map<?, ?> platforms)) {
            return aiResp;
        }

        Map<String, Object> finalPlatforms = new HashMap<>();

        for (Map.Entry<?, ?> entry : platforms.entrySet()) {
            String platform = String.valueOf(entry.getKey());
            Object listObj = entry.getValue();

            if (listObj instanceof List<?> rawList) {
                finalPlatforms.put(platform, selectForPlatform(rawList, currentPlatformId));
            } else {
                finalPlatforms.put(platform, platformResult(Collections.emptyList(), null));
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("platforms", finalPlatforms);
        if (aiResp.containsKey("error")) {
            response.put("error", aiResp.get("error"));
        }
        if (aiResp.containsKey("errors")) {
            response.put("errors", aiResp.get("errors"));
        }
        return response;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> selectForPlatform(List<?> rawList, String currentPlatformId) {
        List<Map<String, Object>> valid = new ArrayList<>();

        for (Object obj : rawList) {
            if (!(obj instanceof Map<?, ?> rawItem)) {
                continue;
            }

            Map<String, Object> item = new HashMap<>((Map<String, Object>) rawItem);

            Double price = item.get("price") instanceof Number p ? p.doubleValue() : null;
            Double confidence = item.get("confidence") instanceof Number c ? c.doubleValue() : null;

            if (price != null && confidence != null) {
                item.put("price", price);
                item.put("confidence", confidence);
                item.put("is_current", isCurrentProduct(item, currentPlatformId));
                valid.add(item);
            }
        }

        if (valid.isEmpty()) {
            return platformResult(Collections.emptyList(), null);
        }

        valid.sort(Comparator
                .comparingDouble((Map<String, Object> m) -> (Double) m.get("confidence")).reversed()
                .thenComparingDouble(m -> (Double) m.get("price"))
                .thenComparing(m -> !(Boolean) m.get("is_current")));

        // 2. Pick lowest price only from candidates in the top confidence tier
        double topConfidence = (Double) valid.get(0).get("confidence");
        double minEligibleConfidence = topConfidence >= HIGH_CONFIDENCE_FLOOR
                ? Math.max(HIGH_CONFIDENCE_FLOOR, topConfidence - CONFIDENCE_BAND)
                : 0.0;

        Map<String, Object> lowest = valid.stream()
                .filter(m -> (Double) m.get("confidence") >= minEligibleConfidence)
                .min(Comparator.comparingDouble(m -> (Double) m.get("price")))
                .orElse(valid.get(0));

        // 3. Keep top N matches and guarantee lowest is included and flagged
        List<Map<String, Object>> topN = new ArrayList<>(
                valid.subList(0, Math.min(MAX_RESULTS_PER_PLATFORM, valid.size())));
        if (topN.stream().noneMatch(it -> it == lowest)) {
            topN.set(topN.size() - 1, lowest);
        }

        for (Map<String, Object> it : topN) {
            it.put("is_lowest", it == lowest);
        }

        return platformResult(topN, lowest);
    }

    private boolean isCurrentProduct(Map<String, Object> item, String currentPlatformId) {
        if (currentPlatformId == null || currentPlatformId.isBlank()) {
            return false;
        }
        Object itemId = item.get("platformId") != null ? item.get("platformId") : item.get("pid");
        return itemId != null && currentPlatformId.equalsIgnoreCase(String.valueOf(itemId));
    }

    private Map<String, Object> platformResult(List<Map<String, Object>> best, Map<String, Object> lowest) {
        Map<String, Object> result = new HashMap<>();
        result.put("best", best);
        result.put("lowest", lowest);
        return result;
    }
}