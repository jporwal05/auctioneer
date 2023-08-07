package com.jpswcons.auctioneer.services;

import com.jpswcons.auctioneer.data.entities.Auction;
import com.jpswcons.auctioneer.data.repositories.AuctionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuctionService {

    private final AuctionRepository auctionRepository;

    private final StatusUpdaterService statusUpdaterService;

    public AuctionService(AuctionRepository auctionRepository,
                          StatusUpdaterService statusUpdaterService) {
        this.auctionRepository = auctionRepository;
        this.statusUpdaterService = statusUpdaterService;
    }

    @Transactional
    public List<Auction> getAuctions() {
        List<Auction> auctions = auctionRepository.findAll();
        // update status to LIVE or FINISHED by comparing against now()
        // this is a workaround to avoid using cron or something similar
        statusUpdaterService.updateStatus(auctions);
        auctionRepository.saveAll(auctions);
        return auctions;
    }

    @Transactional
    public Auction getAuction(long id) {
        Auction auction = auctionRepository.findById(id).orElseThrow();
        // update status to LIVE or FINISHED by comparing against now()
        // this is a workaround to avoid using cron or something similar
        statusUpdaterService.updateStatus(auction);
        auctionRepository.save(auction);
        return auction;
    }
}
