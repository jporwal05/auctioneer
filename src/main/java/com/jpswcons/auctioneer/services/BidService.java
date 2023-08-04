package com.jpswcons.auctioneer.services;

import com.jpswcons.auctioneer.data.entities.Auction;
import com.jpswcons.auctioneer.data.entities.Bid;
import com.jpswcons.auctioneer.data.repositories.BidRepository;
import com.jpswcons.auctioneer.validators.BidValidator;
import com.jpswcons.auctioneer.web.controller.models.BidDto;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

@Service
@Log4j2
public class BidService {

    private final BidRepository bidRepository;

    private final AuctionService auctionService;

    @Autowired
    public BidService(BidRepository bidRepository, AuctionService auctionService) {
        this.bidRepository = bidRepository;
        this.auctionService = auctionService;
    }

    @Transactional
    public boolean placeBid(BidDto bidDto) {
        StopWatch watch = new StopWatch();
        watch.start();
        try {
            Auction auction = auctionService.getAuction(bidDto.getAuctionId());
            if (auction.getStatus().equals(Auction.AuctionStatus.UPCOMING)
                    || auction.getStatus().equals(Auction.AuctionStatus.FINISHED)) {
                return false;
            }
            boolean isValidBid = new BidValidator(auction)
                    .isFirstBid()
                    .isBidAmountGreaterThanPreviousBid(bidDto.getAmount())
                    .isBidAmountGreaterThanMinimumBid(bidDto.getAmount())
                    .isConsecutiveBid(bidDto.getBidderId())
                    .validate();
            if (isValidBid) {
                Bid savedBid = bidRepository.save(Bid.builder()
                        .auctionId(auction.getId())
                        .bidderId(bidDto.getBidderId())
                        .amount(bidDto.getAmount())
                        .build());
                auction.setWinningBid(savedBid);
                return true;
            }
            return false;
        } finally {
            watch.stop();
            log.info("placeBid() took {} ms", watch.getTotalTimeMillis());
        }
    }
}
