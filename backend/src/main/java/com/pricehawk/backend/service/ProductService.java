package com.pricehawk.backend.service;

import com.pricehawk.backend.entity.PriceHistory;
import com.pricehawk.backend.entity.Product;
import com.pricehawk.backend.repository.PriceHistoryRepository;
import com.pricehawk.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//import java.time.LocalDateTime;
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

    @Transactional
    public Product saveOrUpdateProduct(Product product) {
        Optional<Product> existing = productRepository
                .findByPlatformIdAndPlatform(product.getPlatformId(), product.getPlatform());

        if (existing.isPresent()) {
            Product dbProduct = existing.get();

            // Save history before the update, IF price changed
            if (dbProduct.getCurrentPrice() != null
                    && product.getCurrentPrice() != null
                    && dbProduct.getCurrentPrice().compareTo(product.getCurrentPrice()) != 0) {

                PriceHistory history = new PriceHistory();
                history.setProduct(dbProduct);
                history.setPrice(product.getCurrentPrice());
                priceHistoryRepository.save(history);
            }

            // Update product fields
            dbProduct.setCurrentPrice(product.getCurrentPrice());
            dbProduct.setReviewCount(product.getReviewCount());
            dbProduct.setRating(product.getRating());
            dbProduct.setImageUrl(product.getImageUrl());
            dbProduct.setProductUrl(product.getProductUrl());
            dbProduct.setIsTracked(product.getIsTracked());

            return productRepository.save(dbProduct);

        } else {
            // Save new product
            product.setIsTracked(product.getIsTracked());

            Product saved = productRepository.save(product);

            // 🔥 Save initial history
            if (saved.getCurrentPrice() != null) {
                PriceHistory history = new PriceHistory();
                history.setProduct(saved);
                history.setPrice(saved.getCurrentPrice());
                priceHistoryRepository.save(history);
            }

            return saved;
        }
    }

}
