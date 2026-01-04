package com.pricehawk.backend.service;

import com.pricehawk.backend.entity.Product;
import com.pricehawk.backend.payload.response.CompareResponse;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CompareService {

    private final ProductService productService;

    public CompareService(ProductService productService) {
        this.productService = productService;
    }

    public CompareResponse compare(String platform, String platformId) {

        // 1. get current product from DB
        Product current = productService.findByPlatformAndPlatformId(platform, platformId);

        if (current == null) {
            throw new RuntimeException("Product not found in DB");
        }

        double flipkart = current.getCurrentPrice();

        // 2. TEMP – replace these later with Playwright scraper results
        double amazon = flipkart - 1200; // dummy lower price
        double croma = flipkart - 800;

        // 3. map prices
        Map<String, Double> prices = new HashMap<>();
        prices.put("FLIPKART", flipkart);
        prices.put("AMAZON", amazon);
        prices.put("CROMA", croma);

        // 4. find best
        String best = prices.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .get()
                .getKey();

        double difference = flipkart - prices.get(best);

        // 5. build response
        return new CompareResponse(best, difference, prices);
    }
}
