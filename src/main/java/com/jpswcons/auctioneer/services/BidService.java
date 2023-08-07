package com.jpswcons.auctioneer.services;

import com.jpswcons.auctioneer.data.entities.Auction;
import com.jpswcons.auctioneer.data.entities.Bid;
import com.jpswcons.auctioneer.data.repositories.BidRepository;
import com.jpswcons.auctioneer.validators.BidValidator;
import com.jpswcons.auctioneer.web.controller.models.BidDto;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

@Service
@Log4j2
public class BidService {

    private final BidRepository bidRepository;

    private final AuctionService auctionService;

    private final MeterRegistry meterRegistry;



    @Autowired
    public BidService(BidRepository bidRepository,
                      AuctionService auctionService,
                      MeterRegistry meterRegistry) {
        this.bidRepository = bidRepository;
        this.auctionService = auctionService;
        this.meterRegistry = meterRegistry;
    }

    @Transactional
    @Timed("placeBid")
    /*@Retryable(value = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 2, backoff = @Backoff(delay = 50))*/
    public boolean placeBid(BidDto bidDto) {
        Auction auction = auctionService.getAuction(bidDto.getAuctionId());
        if (auction.getStatus().equals(Auction.AuctionStatus.UPCOMING)
                || auction.getStatus().equals(Auction.AuctionStatus.FINISHED)) {
            return false;
        }
        boolean isValidBid = new BidValidator(auction, meterRegistry)
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
    }

    @Transactional
    public Long reconcileBids(long auctionId) {
        List<Bid> bids = bidRepository.findByAuctionIdOrderByCreatedTimeAsc(auctionId).orElseThrow();
        if (bids.size() > 1) {
            log.info("{} bids found for the auction: {}", bids.size(), auctionId);
            if (IntStream.range(0, bids.size() - 1)
                    .allMatch(i -> bids.get(i).getAmount() < bids.get(i + 1).getAmount())) {
                log.info("Reconciliation status: SUCCESS");
                return (long) bids.size();
            } else {
                log.info("Reconciliation status: FAILED");
                return (long) -1;
            }
        } else {
            log.info("{} bids found for the auction: {}", bids.size(), auctionId);
            log.info("Reconciliation status: SUCCESS");
            return (long) bids.size();
        }
    }

    @Transactional
    public Boolean deleteByAuctionId(long auctionId) {
        bidRepository.deleteByAuctionId(auctionId);
        return true;
    }
}
