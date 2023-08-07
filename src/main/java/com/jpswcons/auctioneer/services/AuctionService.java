package com.jpswcons.auctioneer.services;

import com.jpswcons.auctioneer.data.entities.Auction;
import com.jpswcons.auctioneer.data.entities.Bid;
import com.jpswcons.auctioneer.data.repositories.AuctionRepository;
import com.jpswcons.auctioneer.validators.BidValidator;
import com.jpswcons.auctioneer.web.controller.models.BidDto;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

@Service
@Log4j2
public class AuctionService {

    private final AuctionRepository auctionRepository;

    private final StatusUpdaterService statusUpdaterService;

    private final MeterRegistry meterRegistry;

    private final BidService bidService;

    public AuctionService(AuctionRepository auctionRepository,
                          StatusUpdaterService statusUpdaterService,
                          MeterRegistry meterRegistry, BidService bidService) {
        this.auctionRepository = auctionRepository;
        this.statusUpdaterService = statusUpdaterService;
        this.meterRegistry = meterRegistry;
        this.bidService = bidService;
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

    @Transactional
    @Timed("placeBid")
    /*@Retryable(value = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 2, backoff = @Backoff(delay = 50))*/
    public boolean placeBid(long auctionId, BidDto bidDto) {
        Auction auction = auctionRepository.findById(auctionId).orElseThrow();
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
            Bid savedBid = bidService.save(Bid.builder()
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
        List<Bid> bids = bidService.findByAuctionIdOrderByCreatedTimeAsc(auctionId);
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
    public Boolean resetWinningBid(long auctionId) {
        Auction auction = auctionRepository.findById(auctionId).orElseThrow();
        auction.setWinningBid(null);
        auctionRepository.save(auction);
        return true;
    }

    @Transactional(propagation = Propagation.NEVER)
    public Boolean deleteBidsByAuctionId(long auctionId) {
        bidService.deleteByAuctionId(auctionId);
        return true;
    }
}
