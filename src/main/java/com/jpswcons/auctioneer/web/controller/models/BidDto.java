package com.jpswcons.auctioneer.web.controller.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BidDto {
    private long auctionId;
    private long bidderId;
    private int amount;
}
