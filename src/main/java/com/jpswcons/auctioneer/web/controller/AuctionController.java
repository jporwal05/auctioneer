package com.jpswcons.auctioneer.web.controller;

import com.jpswcons.auctioneer.data.entities.Auction;
import com.jpswcons.auctioneer.services.AuctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1/auctions")
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


}
