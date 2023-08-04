package com.jpswcons.auctioneer.services;

import com.jpswcons.auctioneer.data.entities.Auction;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Log4j2
public class StatusUpdaterService {

    public void updateStatus(List<Auction> auctions) {
        auctions.forEach(this::updateStatus);
    }

    public void updateStatus(Auction auction) {
        if (auction.getStatus().equals(Auction.AuctionStatus.UPCOMING)) {
            if (auction.getStartTime().isBefore(LocalDateTime.now())
                    || auction.getStartTime().isEqual(LocalDateTime.now())) {
                auction.setStatus(Auction.AuctionStatus.LIVE);
            }
        } else if (auction.getStatus().equals(Auction.AuctionStatus.LIVE)) {
            if (auction.getEndTime().isBefore(LocalDateTime.now())
                    || auction.getEndTime().isEqual(LocalDateTime.now())) {
                auction.setStatus(Auction.AuctionStatus.FINISHED);
            }
        }
    }
}
