package com.pricehawk.backend.service;

import com.pricehawk.backend.entity.Product;
import com.pricehawk.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class ProductComparisonService {

    @Autowired
    private ProductRepository productRepository;

    /**
     * Compares the given product with its counterpart on the other platform.
     * Returns the product that has the lower price.
     */
    public Product compareAndGetBestDeal(Product product) {
        String otherPlatform = product.getPlatform().equals("AMAZON") ? "FLIPKART" : "AMAZON";
        
        // In a real scenario, we would use a mapping table or fuzzy matching on product titles.
        // For this MVP, we will try to find a product with a similar name in the DB.
        // NOTE: This assumes we have scraped both.
        
        Optional<Product> counterpart = findCounterpart(product, otherPlatform);

        if (counterpart.isPresent()) {
            BigDecimal price1 = product.getCurrentPrice();
            BigDecimal price2 = counterpart.get().getCurrentPrice();
            
            if (price1 != null && price2 != null) {
                return price1.compareTo(price2) <= 0 ? product : counterpart.get();
            }
        }
        
        return product; // If no comparison found, current is default best
    }

    private Optional<Product> findCounterpart(Product product, String targetPlatform) {
        // Simple logic: fuzzy search by name. 
        // Improvement: Use an AI service mapping ID.
        return productRepository.findByNameContainingIgnoreCase(product.getName().split(" ")[0])
                .stream()
                .filter(p -> p.getPlatform().equals(targetPlatform))
                .findFirst();
    }
}
