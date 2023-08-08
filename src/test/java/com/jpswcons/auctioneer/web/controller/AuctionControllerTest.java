package com.jpswcons.auctioneer.web.controller;

import com.jpswcons.auctioneer.TestRestUtils;
import com.jpswcons.auctioneer.web.controller.models.BidDto;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Log4j2
@Disabled
public class AuctionControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    private TestRestUtils testRestUtils;

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    private HashOperations hashOperations;

    @BeforeEach
    void setUp() {
        testRestUtils = new TestRestUtils(port, testRestTemplate);
    }

    @Test
    @DisplayName("should place a bid successfully")
    void shouldPlaceABidSuccessfully() {
        int bidAmount = 215000;
        long auctionId = 2L;

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.get("auctions", "1")).thenReturn(null);

        BidDto bidDto = BidDto.builder()
                .amount(bidAmount)
                .bidderId(1L)
                .build();

        List<Boolean> resultList = new ArrayList<>();
        resultList.add(testRestUtils.sendBidRequest(auctionId, bidDto));

        assertEquals(String.valueOf(resultList.stream().filter(b -> b).count()), testRestUtils.sendBidReconRequest(auctionId));
    }

    @Test
    @DisplayName("should place bids randomly")
    void shouldPlaceBidsRandomly() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        executorService.awaitTermination(2, TimeUnit.SECONDS);

        int bidAmount = 160000;
        int stepPrice = 10000;
        long auctionId = 1L;

        // testing params
        int numberOfBids = 1000;

        // buffering params
        int startingIndex = 500; // starting index of the parallel requests
        int buffer = 100; // number of parallel requests

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

        List<Boolean> resultList = new ArrayList<>();

        for (int i = 0; i < bids.size(); i++) {
            if (i == startingIndex) {
                // buffer next few requests and send in parallel
                for (int pi = startingIndex; pi < startingIndex + buffer; pi++) {
                    BidDto parallelBid = bids.get(pi);
                    executorService.submit(() -> {
                        resultList.add(testRestUtils.sendBidRequest(auctionId, parallelBid));
                    });
                }
                // resume i
                i = startingIndex + buffer;
            } else {
                resultList.add(testRestUtils.sendBidRequest(auctionId, bids.get(i)));
            }
        }

        assertEquals(String.valueOf(resultList.stream().filter(b -> b).count()),
                testRestUtils.sendBidReconRequest(auctionId));

        executorService.shutdown();
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
