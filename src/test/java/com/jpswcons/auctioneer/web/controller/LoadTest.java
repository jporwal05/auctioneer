package com.jpswcons.auctioneer.web.controller;

import com.jpswcons.auctioneer.web.controller.models.BidDto;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
@Log4j2
@Disabled
public class LoadTest {

    // @LocalServerPort
    private int port = 8080;

    @Autowired
    private TestRestTemplate testRestTemplate = new TestRestTemplate();

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
        int numberOfBids = 100000;

        // buffering params
        int startingIndex = 50000; // starting index of the parallel requests
        int buffer = 2000; // number of parallel requests

        List<BidDto> bids = new ArrayList<>();
        for (int i = 0; i < numberOfBids; i++) {
            bids.add(BidDto.builder()
                    .amount(bidAmount)
                    .auctionId(auctionId)
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
                        resultList.add(sendBidRequest(parallelBid));
                    });
                }
                // resume i
                i = startingIndex + buffer - 1;
            } else {
                resultList.add(sendBidRequest(bids.get(i)));
            }
        }

        executorService.shutdown();

        String actual = sendBidReconRequest(auctionId);

        // clean up
        /*sendUpdateWinningBidRequest(auctionId);
        sendDeleteBidsRequest(auctionId);*/

        assertEquals(String.valueOf(resultList.stream().filter(b -> b).count()), actual);
    }

    private boolean sendBidRequest(BidDto bidDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BidDto> entity = new HttpEntity<>(bidDto, headers);
        return Boolean.parseBoolean(testRestTemplate.postForEntity("http://localhost:" +
                port + "/auctioneer/v1/bids", entity, String.class).getBody());
    }

    private String sendBidReconRequest(long auctionId) {
        return testRestTemplate.getForEntity("http://localhost:" +
                port + "/auctioneer/v1/bids/reconcile/" + auctionId, String.class).getBody();
    }

    // FIXME: Need to fix
    private boolean sendDeleteBidsRequest(long auctionId) {
        testRestTemplate.delete("http://localhost:" +
                port + "/auctioneer/v1/bids" + auctionId);
        return true;
    }

    // FIXME: Need to fix
    private boolean sendUpdateWinningBidRequest(long auctionId) {
        return Boolean.parseBoolean(testRestTemplate.getForEntity("http://localhost:" +
                port + "/auctioneer/v1/auctions/delete/winningBid/" + auctionId, String.class).getBody());
    }

    private BidDto lastBid(List<BidDto> bids) {
        return bids.get(bids.size() - 1);
    }

    private static List<Integer> generateRandomNumbers(int numberOfIntegers) {
        Random random = new SecureRandom();
        List<Integer> numbers = new ArrayList<>();

        for(int i = 0; i < numberOfIntegers; i++) {
            int randomNumber = random.nextInt(101);
            numbers.add(randomNumber);
        }
        return numbers;
    }

}
