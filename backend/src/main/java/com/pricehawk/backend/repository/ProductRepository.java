package com.pricehawk.backend.repository;

import com.pricehawk.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByPlatformIdAndPlatform(String platformId, String platform);
    List<Product> findByIsTrackedTrue();
    // For fuzzy search if needed later
    List<Product> findByNameContainingIgnoreCase(String name);
    
}
