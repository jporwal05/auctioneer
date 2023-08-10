package com.jpswcons.auctioneer.utils;

import java.time.Duration;
import java.time.LocalDateTime;

public class AuctioneerUtils {

    private AuctioneerUtils() {

    }


    public static boolean isLastMinuteBid(LocalDateTime endTime) {
        Duration duration =  Duration.between(endTime, LocalDateTime.now());
        return duration.toMinutes() == 0;
    }
}
