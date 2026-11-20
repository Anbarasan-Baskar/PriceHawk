package com.pricehawk.backend.service;

import com.pricehawk.backend.entity.PriceHistory;
import com.pricehawk.backend.entity.Product;
import com.pricehawk.backend.repository.PriceHistoryRepository;
import com.pricehawk.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private PriceHistoryRepository priceHistoryRepository;

    public List<Product> getAllTrackedProducts() {
        return productRepository.findByIsTrackedTrue();
    }

    public Product findByPlatformAndPlatformId(String platform, String platformId) {
        return productRepository
                .findByPlatformIdAndPlatform(platformId, platform)
                .orElse(null);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Optional<Product> getBestDeal(Long id) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) {
            return Optional.empty();
        }
        Product product = productOpt.get();
        if (product.getGroupId() == null || product.getGroupId().isEmpty()) {
            return Optional.of(product);
        }
        List<Product> groupedProducts = productRepository.findByGroupId(product.getGroupId());
        if (groupedProducts == null || groupedProducts.isEmpty()) {
            return Optional.of(product);
        }
        return groupedProducts.stream()
                .filter(p -> p.getCurrentPrice() != null)
                .min(Comparator.comparing(Product::getCurrentPrice));
    }

    @Transactional
    public Optional<Product> updateTrackStatus(String platformId, String platform, Boolean isTracked) {
        return productRepository.findByPlatformIgnoreCaseAndPlatformId(platform, platformId)
                .map(product -> {
                    product.setIsTracked(isTracked != null ? isTracked : true);
                    return productRepository.save(product);
                });
    }

    public Product saveOrUpdateProduct(Product product) {
        if (product.getPlatformId() == null || product.getPlatformId().isBlank()
                || product.getPlatform() == null || product.getPlatform().isBlank()) {
            throw new IllegalArgumentException("platformId and platform are required");
        }

        String platformId = product.getPlatformId().trim();
        String platform = product.getPlatform().trim();

        Optional<Product> existing = productRepository
                .findByPlatformIgnoreCaseAndPlatformId(platform, platformId);

        if (existing.isPresent()) {
            Product saved = existing.get();
            saved.setPlatformId(platformId);
            saved.setPlatform(platform);
            saved.setName(product.getName());
            saved.setImageUrl(product.getImageUrl());
            saved.setProductUrl(product.getProductUrl());
            saved.setCurrentPrice(product.getCurrentPrice());
            saved.setRating(product.getRating());
            saved.setReviewCount(product.getReviewCount());
            if (product.getIsTracked() != null) {
                saved.setIsTracked(product.getIsTracked());
            }
            return productRepository.save(saved);
        }

        product.setPlatformId(platformId);
        product.setPlatform(platform);
        product.setIsTracked(product.getIsTracked() != null ? product.getIsTracked() : false);
        return productRepository.save(product);
    }

}
