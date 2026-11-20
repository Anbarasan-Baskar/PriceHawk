package com.pricehawk.backend.controller;

import com.pricehawk.backend.payload.request.PredictionRequest;
import com.pricehawk.backend.payload.request.PriceHistoryPoint;
import com.pricehawk.backend.payload.response.PredictionResponse;
import com.pricehawk.backend.service.AIClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private AIClientService aiClientService;

    @PostMapping("/predict")
    public PredictionResponse predictPrice(@RequestBody PredictionRequest payload) {
        try {
            double currentPrice = payload.getPrice();
            List<PriceHistoryPoint> history = payload.getHistory();

            if (history == null || history.isEmpty()) {
                history = new ArrayList<>();
                history.add(new PriceHistoryPoint(currentPrice, java.time.LocalDate.now().toString()));
            }

            return aiClientService.getPrediction(currentPrice, history);

        } catch (Exception e) {
            return PredictionResponse.error("Invalid data format");
        }
    }
}
