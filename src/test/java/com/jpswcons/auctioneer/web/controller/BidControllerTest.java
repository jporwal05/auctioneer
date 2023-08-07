package com.jpswcons.auctioneer.web.controller;

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
    }

    @Test
    @DisplayName("should place bids randomly")
    void shouldPlaceBidsRandomly() {
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

        for (int i = 0; i < bids.size(); i++) {
            if (i == 23) {
                // buffer next 5 requests and send in parallel
                ExecutorService executorService = Executors.newFixedThreadPool(5);
                for (int pi = 23; pi < 28; pi++) {
                    BidDto parallelBid = bids.get(pi);
                    executorService.submit(() -> {
                        sendBidRequest(parallelBid);
                    });
                }
                i = 27;
            } else {
                sendBidRequest(bids.get(i));
            }
        }

        assertTrue(bidService.reconcileBids(1L));
    }

    private boolean sendBidRequest(BidDto bidDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BidDto> entity = new HttpEntity<>(bidDto, headers);
        return Boolean.parseBoolean(testRestTemplate.postForEntity("http://localhost:" +
                port + "/auctioneer/v1/bids", entity, String.class).getBody());
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
