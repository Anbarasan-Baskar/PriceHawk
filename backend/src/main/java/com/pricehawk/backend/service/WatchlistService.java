package com.pricehawk.backend.service;

import com.pricehawk.backend.entity.Product;
import com.pricehawk.backend.entity.User;
import com.pricehawk.backend.entity.Watchlist;
import com.pricehawk.backend.repository.ProductRepository;
import com.pricehawk.backend.repository.UserRepository;
import com.pricehawk.backend.repository.WatchlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class WatchlistService {

    @Autowired
    private WatchlistRepository watchlistRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Watchlist> getUserWatchlist(Long userId) {
        return watchlistRepository.findByUserId(userId);
    }

    public Watchlist addToWatchlist(Long userId, Long productId, BigDecimal targetPrice) {
        Optional<Watchlist> existing = watchlistRepository.findByUserIdAndProductId(userId, productId);
        if (existing.isPresent()) {
            return existing.get();
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));

        Watchlist watchlist = new Watchlist();
        watchlist.setUser(user);
        watchlist.setProduct(product);
        watchlist.setTargetPrice(targetPrice);
        
        return watchlistRepository.save(watchlist);
    }

    public void removeFromWatchlist(Long watchlistId) {
        watchlistRepository.deleteById(watchlistId);
    }
}
