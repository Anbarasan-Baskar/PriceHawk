package com.pricehawk.backend.controller;

import com.pricehawk.backend.entity.Product;
import com.pricehawk.backend.service.ProductComparisonService;
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

    @Autowired
    private ProductComparisonService comparisonService;

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
        return productService.getProductById(id)
                .map(p -> ResponseEntity.ok(comparisonService.compareAndGetBestDeal(p)))
                .orElse(ResponseEntity.notFound().build());
    }
}
