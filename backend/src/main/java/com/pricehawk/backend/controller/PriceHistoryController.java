package com.pricehawk.backend.controller;

import com.pricehawk.backend.entity.PriceHistory;
import com.pricehawk.backend.repository.PriceHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/history")
public class PriceHistoryController {

    @Autowired
    private PriceHistoryRepository priceHistoryRepository;

    @GetMapping("/{productId}")
    public List<PriceHistory> getHistory(@PathVariable Long productId) {
        return priceHistoryRepository.findByProductIdOrderByRecordedAtAsc(productId);
    }
}
