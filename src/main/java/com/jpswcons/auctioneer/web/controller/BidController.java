package com.jpswcons.auctioneer.web.controller;

import com.jpswcons.auctioneer.services.BidService;
import com.jpswcons.auctioneer.web.controller.models.BidDto;
import jakarta.persistence.OptimisticLockException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/bid")
@Log4j2
public class BidController {

    private final BidService bidService;

    public BidController(BidService bidService) {
        this.bidService = bidService;
    }

    @PostMapping
    public ResponseEntity<Boolean> placeBid(@RequestBody BidDto bidDto) {
        try {
            bidService.placeBid(bidDto);
        } catch (Exception e) {
            log.error("Error placing bid: {}", e.getMessage());
            if (e instanceof OptimisticLockException) {
                log.error("Bid outdated: {}", e.getMessage());
            }
            return ResponseEntity.ok(false);
        }
        return ResponseEntity.ok(true);
    }
}
