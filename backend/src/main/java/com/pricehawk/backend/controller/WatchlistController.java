package com.pricehawk.backend.controller;

import com.pricehawk.backend.entity.Watchlist;
import com.pricehawk.backend.security.UserDetailsImpl;
import com.pricehawk.backend.service.WatchlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/watchlist")
public class WatchlistController {

    @Autowired
    private WatchlistService watchlistService;

    @GetMapping
    public List<Watchlist> getMyWatchlist(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return watchlistService.getUserWatchlist(userDetails.getId());
    }

    @PostMapping
    public Watchlist addToWatchlist(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestBody Map<String, Object> payload) {
        Long productId = Long.valueOf(payload.get("productId").toString());
        BigDecimal targetPrice = payload.containsKey("targetPrice") ? 
                new BigDecimal(payload.get("targetPrice").toString()) : null;
        
        return watchlistService.addToWatchlist(userDetails.getId(), productId, targetPrice);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeFromWatchlist(@PathVariable Long id) {
        watchlistService.removeFromWatchlist(id);
        return ResponseEntity.ok().build();
    }
}
