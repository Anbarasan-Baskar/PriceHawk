package com.pricehawk.backend.controller;

import com.pricehawk.backend.payload.request.ApifyPriceHistoryRequest;
import com.pricehawk.backend.service.ApifyPriceHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/apify")
public class ApifyController {

    @Autowired
    private ApifyPriceHistoryService apifyPriceHistoryService;

    @PostMapping("/price-history")
    public ResponseEntity<?> importPriceHistory(@RequestBody ApifyPriceHistoryRequest request) {
        if (request == null || request.getIdentifiers() == null || request.getIdentifiers().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "identifiers must not be empty"));
        }

        try {
            return ResponseEntity.ok(apifyPriceHistoryService.fetchAndStorePriceHistory(request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to import Apify price history", "details", e.getMessage()));
        }
    }
}
