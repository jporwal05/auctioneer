package com.jpswcons.auctioneer.services;

import com.jpswcons.auctioneer.data.entities.Bid;
import com.jpswcons.auctioneer.data.repositories.BidRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Log4j2
public class BidService {

    private final BidRepository bidRepository;



    @Autowired
    public BidService(BidRepository bidRepository) {
        this.bidRepository = bidRepository;
    }

    @Transactional
    public Bid save(Bid bid) {
        return bidRepository.save(bid);
    }

    public List<Bid> findByAuctionIdOrderByCreatedTimeAsc(long auctionId) {
        return bidRepository.findByAuctionIdOrderByCreatedTimeAsc(auctionId).orElseThrow();
    }

    @Transactional
    public Boolean deleteByAuctionId(long auctionId) {
        bidRepository.deleteByAuctionId(auctionId);
        return true;
    }
}
