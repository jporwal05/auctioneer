package com.jpswcons.auctioneer.web.controller;

import com.jpswcons.auctioneer.TestRestUtils;
import com.jpswcons.auctioneer.web.controller.models.BidDto;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;

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

        List<Boolean> resultList = new ArrayList<>();

        int bidAmount = 160000;
        int stepPrice = 100;

        // testing params
        int numberOfBids = 10000;

        // buffering params
        int startingIndex = 5000; // starting index of the parallel requests
        int buffer = 200; // number of parallel requests

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

        executorService.shutdown();

        String actual = restUtils.sendBidReconRequest(auctionId);

        // clean up
        restUtils.sendResetWinningBidRequest(auctionId);
        restUtils.sendDeleteBidsRequest(auctionId);

        assertEquals(String.valueOf(resultList.stream().filter(b -> b).count()), actual);
    }

}
