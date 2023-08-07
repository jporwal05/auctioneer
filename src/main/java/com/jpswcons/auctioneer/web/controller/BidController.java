package com.jpswcons.auctioneer.web.controller;

import com.jpswcons.auctioneer.services.BidService;
import com.jpswcons.auctioneer.web.controller.models.BidDto;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/bids")
@Log4j2
public class BidController {

    private final BidService bidService;

    public BidController(BidService bidService) {
        this.bidService = bidService;
    }

    @PostMapping
    public ResponseEntity<Boolean> placeBid(@RequestBody BidDto bidDto) {
        try {
            return ResponseEntity.ok(bidService.placeBid(bidDto));
        } catch (Exception e) {
            if (e instanceof ObjectOptimisticLockingFailureException) {
                log.error("Bid outdated: {}", e.getMessage());
            } else {
                log.error("Error placing bid: {}", e.getMessage());
            }
        }
        return ResponseEntity.ok(false);
    }
}
