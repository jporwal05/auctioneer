package com.jpswcons.auctioneer.validators;

import com.jpswcons.auctioneer.data.entities.Auction;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.log4j.Log4j2;

import java.util.Optional;

@Log4j2
public class BidValidator {

    private final Auction auction;

    private boolean isValidBid = true;

    private boolean isFirstBid = false;

    private final MeterRegistry meterRegistry;

    private final Counter greaterThanPreviousBidCounter;

    public BidValidator(Auction auction, MeterRegistry meterRegistry) {
        this.auction = auction;
        this.meterRegistry = meterRegistry;
        this.greaterThanPreviousBidCounter = meterRegistry.counter("gtpbid_validation");
    }


    public BidValidator isFirstBid() {
        if (isValidBid) {
            Optional.ofNullable(auction.getWinningBid()).ifPresentOrElse(b -> {}, () -> isFirstBid = true);
        }
        return this;
    }


    public BidValidator isBidAmountGreaterThanPreviousBid(int amount) {
        if (isValidBid && !isFirstBid) {
            Optional.ofNullable(auction.getWinningBid()).ifPresent(b -> {
                if (amount < b.getAmount()) {
                    greaterThanPreviousBidCounter.increment();
                }
            });
        }
        return this;
    }


    public BidValidator isBidAmountGreaterThanMinimumBid(int amount) {
        if (isValidBid) {
            if (!isFirstBid) {
                Optional.ofNullable(auction.getWinningBid())
                        .ifPresent(b -> isValidBid = amount >= b.getAmount() + auction.getStepPrice());
            } else {
                isValidBid = amount >= auction.getStartingPrice() + auction.getStepPrice();
            }
        }
        return this;
    }


    public BidValidator isConsecutiveBid(long bidderId) {
        if (isValidBid && !isFirstBid) {
            Optional.ofNullable(auction.getWinningBid()).ifPresent(b -> isValidBid = bidderId != b.getBidderId());
        }
        return this;
    }


    public boolean validate() {
        return isValidBid;
    }
}
