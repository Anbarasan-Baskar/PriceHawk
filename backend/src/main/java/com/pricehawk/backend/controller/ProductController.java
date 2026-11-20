package com.pricehawk.backend.controller;

import com.pricehawk.backend.entity.Product;
import com.pricehawk.backend.payload.response.PredictionResponse;
import com.pricehawk.backend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/products")
public class ProductController {

     @Autowired
    private ProductService productService;
    
    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllTrackedProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Product addProduct(@RequestBody Product product) {
        return productService.saveOrUpdateProduct(product);
    }

    @GetMapping("/{id}/best-deal")
    public ResponseEntity<Product> getBestDeal(@PathVariable Long id) {
        return productService.getBestDeal(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @Autowired
    private org.springframework.web.client.RestTemplate restTemplate;

    @org.springframework.beans.factory.annotation.Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    @GetMapping("/{id}/predict")
    public ResponseEntity<java.util.Map> getPrediction(@PathVariable Long id) {
        java.util.Map<String, Object> fallback = java.util.Map.of("confidence", "High", "trend", "Drop", "price", 0);
        try {
            java.util.Map resp = restTemplate.getForObject(aiServiceUrl + "/predict/price?id=" + id, java.util.Map.class);
            return ResponseEntity.ok(resp != null ? resp : fallback);
        } catch (Exception e) {
            return ResponseEntity.ok(fallback);
        }
    }
}
