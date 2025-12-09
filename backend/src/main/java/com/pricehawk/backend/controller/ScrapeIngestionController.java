package com.pricehawk.backend.controller;

import com.pricehawk.backend.payload.request.ScrapeResult;
import com.pricehawk.backend.entity.Product;
import com.pricehawk.backend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scrape")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ScrapeIngestionController {

    @Autowired
    private ProductService productService;

    @PostMapping("/update")
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
        product.setIsTracked(scrape.getIsTracked() != null ? scrape.getIsTracked() : false);


        return productService.saveOrUpdateProduct(product);
    }
}
