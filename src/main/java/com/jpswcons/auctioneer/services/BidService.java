package com.jpswcons.auctioneer.services;

import com.jpswcons.auctioneer.data.entities.Auction;
import com.jpswcons.auctioneer.data.entities.Bid;
import com.jpswcons.auctioneer.data.repositories.BidRepository;
import com.jpswcons.auctioneer.validators.BidValidator;
import com.jpswcons.auctioneer.web.controller.models.BidDto;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
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
                    .auction(auction)
                    .bidderId(bidDto.getBidderId())
                    .amount(bidDto.getAmount())
                    .build());
            auction.setWinningBid(savedBid);
            return true;
        }
        return false;
    }
}
