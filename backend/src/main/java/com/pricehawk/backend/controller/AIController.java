package com.pricehawk.backend.controller;

import com.pricehawk.backend.service.AIClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private AIClientService aiClientService;

    @PostMapping("/predict")
    public Map<String, Object> predictPrice(@RequestBody Map<String, Object> payload) {
        // Payload from extension: { price: "999", history: [] }
        // We need to adapt it for the AI service if necessary, or just pass it through.

        try {
            double currentPrice = Double.parseDouble(payload.get("price").toString());
            List<Map<String, Object>> history = (List<Map<String, Object>>) payload.get("history");

            if (history == null || history.isEmpty()) {
                // If no history provided, create a dummy history point for now to avoid AI
                // error
                // In a real scenario, we would fetch history from our DB
                Map<String, Object> point = Map.of("price", currentPrice, "date", java.time.LocalDate.now().toString());
                history = new ArrayList<>();
                history.add(point);
            }

            return aiClientService.getPrediction(currentPrice, history);

        } catch (Exception e) {
            return Map.of("error", "Invalid data format");
        }
    }
}
