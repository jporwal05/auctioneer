package com.jpswcons.auctioneer.validators;

import com.jpswcons.auctioneer.data.entities.Auction;

import java.util.Optional;

public class BidValidator {

    private final Auction auction;

    private boolean isValidBid = true;

    private boolean isFirstBid = false;

    public BidValidator(Auction auction) {
        this.auction = auction;
    }


    public BidValidator isFirstBid() {
        if (isValidBid) {
            Optional.ofNullable(auction.getWinningBid()).ifPresent(b -> isFirstBid = true);
        }
        return this;
    }


    public BidValidator isBidAmountGreaterThanPreviousBid(int amount) {
        if (isValidBid && !isFirstBid) {
            Optional.ofNullable(auction.getWinningBid()).ifPresent(b -> isValidBid = amount > b.getAmount());
        }
        return this;
    }


    public BidValidator isBidAmountGreaterThanMinimumBid(int amount) {
        if (isValidBid) {
            if (!isFirstBid) {
                Optional.ofNullable(auction.getWinningBid())
                        .ifPresent(b -> isValidBid = amount >= b.getAmount() + b.getAuction().getStepPrice());
            } else {
                isValidBid = amount >= auction.getStartingPrice() + auction.getStepPrice();
            }
        }
        return null;
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
