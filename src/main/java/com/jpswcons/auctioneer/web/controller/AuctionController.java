package com.jpswcons.auctioneer.web.controller;

import com.jpswcons.auctioneer.data.entities.Auction;
import com.jpswcons.auctioneer.services.AuctionService;
import com.jpswcons.auctioneer.web.controller.models.BidDto;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1/auctions")
@Log4j2
public class AuctionController {

    private final AuctionService auctionService;

    private final MeterRegistry meterRegistry;

    private final Counter outdatedBidCounter;

    private final Counter successfulBidsCounter;

    private final Counter failedBidCounter;

    @Autowired
    public AuctionController(AuctionService auctionService, MeterRegistry meterRegistry) {
        this.auctionService = auctionService;
        this.meterRegistry = meterRegistry;
        this.outdatedBidCounter = meterRegistry.counter("outdated_bid");
        this.successfulBidsCounter = meterRegistry.counter("successful_bid");
        this.failedBidCounter = meterRegistry.counter("failed_bid");
    }

    @GetMapping
    public ResponseEntity<List<Auction>> getAuctions() {
        List<Auction> auctions = auctionService.getAuctions();
        return ResponseEntity.of(Optional.of(auctions));
    }

    @PostMapping("/{auctionId}/bid")
    public ResponseEntity<Boolean> placeBid(@PathVariable String auctionId, @RequestBody BidDto bidDto) {
        try {
            boolean bidPlaced = auctionService.placeBid(Long.parseLong(auctionId), bidDto);
            if (bidPlaced) {
                successfulBidsCounter.increment();
            } else {
                failedBidCounter.increment();
            }
            return ResponseEntity.ok(bidPlaced);
        } catch (Exception e) {
            if (e instanceof ObjectOptimisticLockingFailureException) {
                log.warn("Bid outdated: {}", e.getMessage());
                outdatedBidCounter.increment();
            } else {
                log.error("Error placing bid: {}", e.getMessage());
            }
        }
        return ResponseEntity.ok(false);
    }

    @PostMapping("/{auctionId}/resetWinningBid")
    public ResponseEntity<Boolean> updateWinningBid(@PathVariable String auctionId) {
        log.info("Updating winning bid for auction: {}", auctionId);
        return ResponseEntity.ok(auctionService.resetWinningBid(Long.parseLong(auctionId)));
    }

    @GetMapping("/{auctionId}/reconcile")
    public ResponseEntity<String> reconcileBids(@PathVariable String auctionId) {
        return ResponseEntity.ok(String.valueOf(auctionService.reconcileBids(Long.parseLong(auctionId)).longValue()));
    }

    @DeleteMapping("/{auctionId}/bids")
    public ResponseEntity<Boolean> deleteBids(@PathVariable String auctionId) {
        log.info("Deleting all bids for auction: {}", auctionId);
        return ResponseEntity.ok(auctionService.deleteBidsByAuctionId(Long.parseLong(auctionId)));
    }


}
