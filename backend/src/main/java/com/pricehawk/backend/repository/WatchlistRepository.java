package com.pricehawk.backend.repository;

import com.pricehawk.backend.entity.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {
    List<Watchlist> findByUserId(Long userId);
    Optional<Watchlist> findByUserIdAndProductId(Long userId, Long productId);
    List<Watchlist> findByProductIdAndIsActiveTrue(Long productId);
}
