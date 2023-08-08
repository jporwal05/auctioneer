package com.jpswcons.auctioneer.data.repositories;

import com.jpswcons.auctioneer.data.entities.Auction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@EnableCaching
class AuctionRepositoryTest {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private AuctionRepository repository;

    @BeforeEach
    void setUp() {
        repository.save(Auction.builder()
                        .id(1L)
                        .status(Auction.AuctionStatus.LIVE)
                .build());
    }

    @Test
    @DisplayName("should get from cache")
    void shouldGetFromCache() {
        Optional<Auction> auction = repository.findById(1L);

        assertTrue(auction.isPresent());
    }

    @Test
    @DisplayName("should save and put in cache")
    void shouldSaveAndPutInCache() {
        Optional<Auction> auction = repository.findById(1L);

        auction.ifPresent(a -> {
            a.setStatus(Auction.AuctionStatus.FINISHED);
            repository.save(a);
        });

        Optional<Auction> result = repository.findById(1L);
        if (result.isPresent()) {
            assertEquals(Auction.AuctionStatus.FINISHED, result.get().getStatus());
        } else {
            throw new RuntimeException("Cache put not working");
        }
    }


}