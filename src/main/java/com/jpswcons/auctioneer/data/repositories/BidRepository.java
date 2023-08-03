package com.jpswcons.auctioneer.data.repositories;

import com.jpswcons.auctioneer.data.entities.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {
}