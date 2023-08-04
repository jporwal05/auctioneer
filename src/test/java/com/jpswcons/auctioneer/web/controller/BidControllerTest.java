package com.jpswcons.auctioneer.web.controller;

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
import java.util.concurrent.Future;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Log4j2
public class BidControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    @DisplayName("should place a bid successfully")
    void shouldPlaceABidSuccessfully() {
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
                List<Future<Boolean>> futures = new ArrayList<>();
                for (int pi = 23; pi < 28; pi++) {
                    BidDto b = bids.get(pi);
                    final int reqNumber = pi;
                    futures.add(executorService.submit(() -> {
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        HttpEntity<BidDto> entity = new HttpEntity<>(b ,headers);
                        boolean result = Boolean.parseBoolean(testRestTemplate.postForEntity("http://localhost:" + port + "/auctioneer/v1/bids", entity, String.class).getBody());
                        log.info("Request number {} result: {}", reqNumber, result);
                        return result;
                    }));
                }
                i = 27;
            } else {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<BidDto> entity = new HttpEntity<>(bids.get(i), headers);
                boolean result = Boolean.parseBoolean(testRestTemplate.postForEntity("http://localhost:" + port + "/auctioneer/v1/bids", entity, String.class).getBody());
                log.info("Request number {} result: {}", i, result);
            }
        }
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
