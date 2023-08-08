package com.jpswcons.auctioneer.data.repositories;

import com.jpswcons.auctioneer.data.entities.Auction;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {

    @Cacheable(value = "auctions", key = "#auctionId")
    Optional<Auction> findById(Long auctionId);


    @CachePut(value = "auctions", key = "#result.getId()")
    Auction save(Auction auction);

}
