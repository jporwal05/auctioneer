package com.jpswcons.auctioneer.web.controller;

import com.jpswcons.auctioneer.services.BidService;
import com.jpswcons.auctioneer.web.controller.models.BidDto;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/bids")
@Log4j2
public class BidController {

    private final BidService bidService;

    private final MeterRegistry meterRegistry;

    private final Counter outdatedBidCounter;

    private final Counter successfulBidsCounter;
    private final Counter failedBidCounter;

    public BidController(BidService bidService, MeterRegistry meterRegistry) {
        this.bidService = bidService;
        this.meterRegistry = meterRegistry;
        this.outdatedBidCounter = meterRegistry.counter("outdated_bid");
        this.successfulBidsCounter = meterRegistry.counter("successful_bid");
        this.failedBidCounter = meterRegistry.counter("failed_bid");
    }

    @PostMapping
    public ResponseEntity<Boolean> placeBid(@RequestBody BidDto bidDto) {
        try {
            boolean bidPlaced = bidService.placeBid(bidDto);
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

    @GetMapping("/reconcile/{auctionId}")
    public ResponseEntity<String> reconcileBids(@PathVariable String auctionId) {
        return ResponseEntity.ok(String.valueOf(bidService.reconcileBids(Long.parseLong(auctionId)).longValue()));
    }

    @DeleteMapping("/{auctionId}")
    public ResponseEntity<Boolean> deleteBids(@PathVariable String auctionId) {
        log.info("Deleting all bids for auction: {}", auctionId);
        return ResponseEntity.ok(bidService.deleteByAuctionId(Long.parseLong(auctionId)));
    }
}
