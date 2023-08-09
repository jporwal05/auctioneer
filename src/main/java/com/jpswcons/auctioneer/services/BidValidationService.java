package com.jpswcons.auctioneer.services;

import com.jpswcons.auctioneer.data.entities.Auction;
import com.jpswcons.auctioneer.validators.BidValidator;
import com.jpswcons.auctioneer.web.controller.models.BidDto;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BidValidationService {

    private final MeterRegistry meterRegistry;

    @Autowired
    public BidValidationService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }


    public boolean validate(Auction auction, BidDto bidDto) {
        return new BidValidator(auction, meterRegistry)
                .isFirstBid()
                .isBidAmountGreaterThanPreviousBid(bidDto.getAmount())
                .isBidAmountGreaterThanMinimumBid(bidDto.getAmount())
                .isConsecutiveBid(bidDto.getBidderId())
                .validate();
    }
}
