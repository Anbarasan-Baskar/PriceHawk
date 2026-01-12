package com.pricehawk.backend.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class CompareServiceTest {

    @SuppressWarnings("unchecked")
    @Test
    void compareByTitle_selectsTopAndLowest() {
        RestTemplate rest = Mockito.mock(RestTemplate.class);
        CompareService service = new CompareService(rest, "http://localhost:8000");

        // Prepare fake AI response: two platforms, each with candidate lists
        Map<String, Object> aiResp = Map.of(
                "platforms", Map.of(
                        "AMAZON", List.of(
                                Map.of("name", "A1", "price", 500, "confidence", 0.9),
                                Map.of("name", "A2", "price", 450, "confidence", 0.4),
                                Map.of("name", "A3", "price", 700, "confidence", 0.6)
                        ),
                        "FLIPKART", List.of(
                                Map.of("name", "F1", "price", 300, "confidence", 0.8),
                                Map.of("name", "F2", "price", 250, "confidence", 0.5)
                        )
                )
        );

        when(rest.postForObject(eq("http://localhost:8000/instant-compare"), any(), eq(Map.class)))
                .thenReturn(aiResp);

        Map<String, Object> res = service.compareByTitle("query");
        assertNotNull(res);
        assertTrue(res.containsKey("platforms"));

        Map<String, Object> platforms = (Map<String, Object>) res.get("platforms");
        assertTrue(platforms.containsKey("AMAZON"));
        assertTrue(platforms.containsKey("FLIPKART"));

        Map<String, Object> amazon = (Map<String, Object>) platforms.get("AMAZON");
        List<Map<String, Object>> amazonBest = (List<Map<String, Object>>) amazon.get("best");
        Map<String, Object> amazonLowest = (Map<String, Object>) amazon.get("lowest");

        assertEquals(3, amazonBest.size());
        assertNotNull(amazonLowest);
        assertTrue(amazonBest.stream().anyMatch(m -> m.get("is_lowest") != null && (Boolean)m.get("is_lowest")));

        Map<String, Object> flip = (Map<String, Object>) platforms.get("FLIPKART");
        List<Map<String, Object>> flipBest = (List<Map<String, Object>>) flip.get("best");
        Map<String, Object> flipLowest = (Map<String, Object>) flip.get("lowest");

        assertEquals(2, flipBest.size());
        assertNotNull(flipLowest);
        assertTrue(flipBest.stream().anyMatch(m -> m.get("is_lowest") != null && (Boolean)m.get("is_lowest")));
    }
}
