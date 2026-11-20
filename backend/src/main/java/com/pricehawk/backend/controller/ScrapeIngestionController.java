package com.pricehawk.backend.controller;

import com.pricehawk.backend.entity.Product;
import com.pricehawk.backend.payload.request.ScrapeResult;
import com.pricehawk.backend.service.ProductService;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scrape")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ScrapeIngestionController {

    @Autowired
    private ProductService productService;

    @PatchMapping("/track")
    public ResponseEntity<?> updateTrackStatus(
            @RequestParam String pid,
            @RequestParam String platform,
            @RequestParam(defaultValue = "true") Boolean isTracked) {

        if (pid.isBlank() || platform.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "pid and platform are required"));
        }

        Optional<Product> updated = productService.updateTrackStatus(pid.trim(), platform.trim(), isTracked);

        if (updated.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "Product not found",
                    "pid", pid,
                    "platform", platform));
        }

        return ResponseEntity.ok(updated.get());
    }

    @PostMapping("/save")
    public Product ingestScrape(@RequestBody ScrapeResult scrape) {

        Product product = new Product();
        product.setPlatformId(scrape.getPlatformId());
        product.setPlatform(scrape.getPlatform());
        product.setName(scrape.getName());
        product.setCurrentPrice(scrape.getCurrentPrice());
        product.setImageUrl(scrape.getImageUrl());
        product.setProductUrl(scrape.getProductUrl());
        product.setRating(scrape.getRating());
        product.setReviewCount(scrape.getReviewCount());
        product.setIsTracked(scrape.getIsTracked());

        return productService.saveOrUpdateProduct(product);
    }

}