package com.jpswcons.auctioneer.data.repositories;

import com.jpswcons.auctioneer.data.entities.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    Optional<List<Bid>> findByAuctionIdOrderByCreatedTimeAsc(Long auctionId);

    void deleteByAuctionId(Long auctionId);


}
