package com.jpswcons.auctioneer.data.repositories;

import com.jpswcons.auctioneer.data.entities.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {

}
