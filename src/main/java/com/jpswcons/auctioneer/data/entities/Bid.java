package com.jpswcons.auctioneer.data.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class Bid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "auction_id", insertable = false, updatable = false)
    private Auction auction;
    @Column(name = "auction_id")
    private Long auctionId;
    private Long bidderId;
    private Long amount;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
