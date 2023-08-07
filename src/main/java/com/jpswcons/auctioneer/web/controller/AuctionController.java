package com.jpswcons.auctioneer.web.controller;

import com.jpswcons.auctioneer.data.entities.Auction;
import com.jpswcons.auctioneer.services.AuctionService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1/auctions")
@Log4j2
public class AuctionController {

    private final AuctionService auctionService;

    @Autowired
    public AuctionController(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @GetMapping
    public ResponseEntity<List<Auction>> getAuctions() {
        List<Auction> auctions = auctionService.getAuctions();
        return ResponseEntity.of(Optional.of(auctions));
    }

    @GetMapping("/delete/winningBid/{auctionId}")
    public ResponseEntity<Boolean> updateWinningBid(@PathVariable String auctionId) {
        log.info("Updating winning bid for auction: {}", auctionId);
        return ResponseEntity.ok(auctionService.deleteWinningBid(Long.parseLong(auctionId)));
    }


}
