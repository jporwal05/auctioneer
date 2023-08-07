package com.jpswcons.auctioneer.web.controller;

import com.jpswcons.auctioneer.services.AuctionService;
import com.jpswcons.auctioneer.services.BidService;
import com.jpswcons.auctioneer.web.controller.models.BidDto;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Log4j2
public class BidControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private BidService bidService;

    @Autowired
    private AuctionService auctionService;

    @Test
    @DisplayName("should place a bid successfully")
    void shouldPlaceABidSuccessfully() {
        int bidAmount = 160000;

        BidDto bidDto = BidDto.builder()
                .amount(bidAmount)
                .auctionId(1L)
                .bidderId(1L)
                .build();

        assertTrue(sendBidRequest(bidDto));
        assertTrue(bidService.reconcileBids(1L));
        assertEquals(bidDto, auctionService.getWinningBid(1L));
    }

    @Test
    @DisplayName("should place bids randomly")
    void shouldPlaceBidsRandomly() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        executorService.awaitTermination(1, TimeUnit.SECONDS);

        int bidAmount = 160000;
        int stepPrice = 10000;

        List<BidDto> bids = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            bids.add(BidDto.builder()
                    .amount(bidAmount)
                    .auctionId(1L)
                    .bidderId(i + 1L)
                    .build());
            bidAmount = bidAmount + stepPrice;
        }

        // buffering params
        int buffer = 20;
        int startingIndex = 23;

        // required because of last bid assertion and concurrency
        assert (bids.size() - 10) > startingIndex + buffer;

        for (int i = 0; i < bids.size(); i++) {
            if (i == startingIndex) {
                // buffer next 5 requests and send in parallel
                for (int pi = startingIndex; pi < startingIndex + buffer; pi++) {
                    BidDto parallelBid = bids.get(pi);
                    executorService.submit(() -> {
                        sendBidRequest(parallelBid);
                    });
                }
                // resume i
                i = startingIndex + buffer - 1;
            } else {
                sendBidRequest(bids.get(i));
            }
        }

        assertTrue(bidService.reconcileBids(1L));
        // TODO: Improve this validation
        assertEquals(lastBid(bids), auctionService.getWinningBid(1L));

        executorService.shutdown();
    }

    private boolean sendBidRequest(BidDto bidDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BidDto> entity = new HttpEntity<>(bidDto, headers);
        return Boolean.parseBoolean(testRestTemplate.postForEntity("http://localhost:" +
                port + "/auctioneer/v1/bids", entity, String.class).getBody());
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
