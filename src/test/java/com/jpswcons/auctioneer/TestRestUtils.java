package com.jpswcons.auctioneer;

import com.jpswcons.auctioneer.data.entities.Auction;
import com.jpswcons.auctioneer.web.controller.models.BidDto;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class TestRestUtils {

    private final int port;

    private final TestRestTemplate testRestTemplate;


    public TestRestUtils(int port, TestRestTemplate testRestTemplate) {
        this.port = port;
        this.testRestTemplate = testRestTemplate;
    }

    public boolean sendBidRequest(long auctionId, BidDto bidDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BidDto> entity = new HttpEntity<>(bidDto, headers);
        return Boolean.parseBoolean(testRestTemplate.postForEntity("http://localhost:" +
                port + "/auctioneer/v1/auctions/" + auctionId + "/bid", entity, String.class).getBody());
    }

    public String sendBidReconRequest(long auctionId) {
        return testRestTemplate.getForEntity("http://localhost:" +
                port + "/auctioneer/v1/auctions/" + auctionId + "/reconcile", String.class).getBody();
    }

    public Auction getAuction(long auctionId) {
        return testRestTemplate.getForEntity("http://localhost:" +
                port + "/auctioneer/v1/auctions/" + auctionId, Auction.class).getBody();
    }

    public boolean sendDeleteBidsRequest(long auctionId) {
        testRestTemplate.delete("http://localhost:" +
                port + "/auctioneer/v1/auctions/" + auctionId + "/bids");
        return true;
    }

    public boolean sendResetWinningBidRequest(long auctionId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BidDto> entity = new HttpEntity<>(null, headers);
        return Boolean.parseBoolean(testRestTemplate.postForEntity("http://localhost:" +
                port + "/auctioneer/v1/auctions/" + auctionId + "/resetWinningBid", entity, String.class).getBody());
    }
}
