package com.jpswcons.auctioneer.data.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Version;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity(name = "auctions")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Auction implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne
    @JoinColumn(name = "company_id")
    @JsonIgnore
    @ToString.Exclude
    private Company company;
    private String itemName;
    private int startingPrice;
    private int stepPrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    @Enumerated(EnumType.STRING)
    private AuctionStatus status;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winning_bid_id")
    private Bid winningBid;
    @CreationTimestamp
    private LocalDateTime createdTime;
    @UpdateTimestamp
    private LocalDateTime updatedTime;
    @Version
    private Long version;

    public enum AuctionStatus {
        UPCOMING, LIVE, FINISHED
    }
}
