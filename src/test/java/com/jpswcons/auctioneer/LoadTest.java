package com.jpswcons.auctioneer;

import com.jpswcons.auctioneer.data.entities.Auction;
import com.jpswcons.auctioneer.web.controller.models.BidDto;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.util.StopWatch;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
@Log4j2
@Disabled
public class LoadTest {

    private final TestRestUtils restUtils = new TestRestUtils(8080,new TestRestTemplate());


    @Test
    @DisplayName("should place bids randomly")
    void shouldPlaceBidsRandomly() throws InterruptedException {
        long auctionId = 1L;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        // get auction start and end times
        Auction auction = restUtils.getAuction(auctionId);
        final LocalDateTime auctionInitialEndTime = auction.getEndTime();

        List<Boolean> resultList = new ArrayList<>();

        int bidAmount = 160000;
        int stepPrice = 100;

        // testing params
        int numberOfBids = 100000;

        // buffering params
        int startingIndex = 50000; // starting index of the parallel requests
        int buffer = 20000; // number of parallel requests

        List<BidDto> bids = new ArrayList<>();
        for (int i = 0; i < numberOfBids; i++) {
            bids.add(BidDto.builder()
                    .amount(bidAmount)
                    .bidderId(i + 1L)
                    .build());
            bidAmount = bidAmount + stepPrice;
        }

        // required because of last bid assertion and concurrency
        assert (numberOfBids - 10) > startingIndex + buffer;

        StopWatch watch = new StopWatch();
        watch.start();
        long startTimeMillis = System.currentTimeMillis();
        for (int i = 0; i < bids.size(); i++) {
            if (i == startingIndex) {
                // buffer next few requests and send in parallel
                for (int pi = startingIndex; pi < startingIndex + buffer; pi++) {
                    BidDto parallelBid = bids.get(pi);
                    // for better race condition
                    parallelBid.setAmount(parallelBid.getAmount());
                    executorService.submit(() -> {
                        resultList.add(restUtils.sendBidRequest(auctionId, parallelBid));
                    });
                }
                // resume i
                i = startingIndex + buffer;
            } else {
                resultList.add(restUtils.sendBidRequest(auctionId, bids.get(i)));
            }
        }
        watch.stop();
        long timeTakenMillis = watch.getTotalTimeMillis();
        log.info("Start time: {}",
                LocalDateTime.ofInstant(Instant.ofEpochMilli(startTimeMillis),
                        ZoneId.of("GMT")));
        log.info("End time: {}", LocalDateTime.ofInstant(Instant.ofEpochMilli(startTimeMillis + timeTakenMillis),
                ZoneId.of("GMT" )));
        log.info("Total time taken: {}", watch.getTotalTimeMillis());

        executorService.shutdown();

        String actual = restUtils.sendBidReconRequest(auctionId);
        LocalDateTime auctionFinalEndTime = restUtils.getAuction(auctionId).getEndTime();

        log.info("Auction end times: {} to {}", auctionInitialEndTime, auctionFinalEndTime);

        // clean up
        restUtils.sendResetWinningBidRequest(auctionId);
        restUtils.sendDeleteBidsRequest(auctionId);

        assertEquals(String.valueOf(resultList.stream().filter(b -> b).count()), actual);
    }

    @Test
    void sample() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusMinutes(2);
        LocalDateTime current = LocalDateTime.now().plusSeconds(59);

        log.info(Duration.between(end, current).toMinutes());
    }
}
