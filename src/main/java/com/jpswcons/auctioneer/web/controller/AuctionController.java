package com.jpswcons.auctioneer.web.controller;

import com.jpswcons.auctioneer.data.entities.Auction;
import com.jpswcons.auctioneer.services.AuctionService;
import com.jpswcons.auctioneer.services.BidValidationService;
import com.jpswcons.auctioneer.services.CompanyService;
import com.jpswcons.auctioneer.web.controller.models.BidDto;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.jpswcons.auctioneer.utils.AuctioneerUtils.isLastMinuteBid;

@RestController
@RequestMapping("/v1/auctions")
@Log4j2
public class AuctionController {

    private final AuctionService auctionService;

    private final MeterRegistry meterRegistry;

    private final Counter outdatedBidCounter;

    private final Counter successfulBidsCounter;

    private final Counter failedBidCounter;

    private final Counter failedBidRedisCounter;

    private final RedisTemplate<String, Object> redisTemplate;

    private final BidValidationService bidValidationService;

    private final Counter endTimeIncreaseCounter;

    private final CompanyService companyService;



    @Autowired
    public AuctionController(AuctionService auctionService, MeterRegistry meterRegistry,
                             RedisTemplate<String, Object> redisTemplate,
                             BidValidationService bidValidationService, CompanyService companyService) {
        this.auctionService = auctionService;
        this.meterRegistry = meterRegistry;
        this.outdatedBidCounter = meterRegistry.counter("outdated_bid");
        this.successfulBidsCounter = meterRegistry.counter("successful_bid");
        this.failedBidCounter = meterRegistry.counter("failed_bid");
        this.failedBidRedisCounter = meterRegistry.counter("failed_redis_bid");
        this.endTimeIncreaseCounter = meterRegistry.counter("end_time_increase");
        this.redisTemplate = redisTemplate;
        this.bidValidationService = bidValidationService;
        this.companyService = companyService;
    }

    @GetMapping
    public ResponseEntity<List<Auction>> getAuctions() {
        List<Auction> auctions = auctionService.getAuctions();
        return ResponseEntity.of(Optional.of(auctions));
    }

    @GetMapping("/{auctionId}")
    public ResponseEntity<Auction> getAuction(@PathVariable String auctionId) {
        Auction auction = auctionService.getAuction(Long.parseLong(auctionId));
        return ResponseEntity.of(Optional.of(auction));
    }

    @PostMapping("/{auctionId}/bid")
    public ResponseEntity<Boolean> placeBid(@PathVariable String auctionId, @RequestBody BidDto bidDto) {
        try {
            // use redis to filter out invalid bids right away
            // saves db cost
            Auction redisAuction = (Auction) redisTemplate.opsForHash().get(auctionId, auctionId);
            if (redisAuction != null && isLastMinuteBid(redisAuction.getEndTime())) {
                if (companyService.increaseEndTime(redisAuction.getCompany().getId(), Duration.of(2, ChronoUnit.MINUTES))) {
                    endTimeIncreaseCounter.increment();
                }
            }
            boolean reVerify = preValidate(redisAuction, bidDto);
            if (reVerify) {
                Optional<Auction> auction = auctionService.placeBid(Long.parseLong(auctionId), bidDto);
                if (auction.isPresent()) {
                    redisTemplate.opsForHash().put(auctionId, auctionId, auction.get());
                    redisTemplate.expire(auctionId, 60, TimeUnit.SECONDS);
                    successfulBidsCounter.increment();
                } else {
                    failedBidCounter.increment();
                }
                return ResponseEntity.ok(auction.isPresent());
            }
        } catch (Exception e) {
            if (e instanceof ObjectOptimisticLockingFailureException) {
                log.warn("Bid outdated: {}", e.getMessage());
                outdatedBidCounter.increment();
            } else {
                log.error("Error placing bid", e);
            }
        }
        return ResponseEntity.ok(false);
    }

    private boolean preValidate(Auction auction, BidDto bidDto) {
        if (auction != null) {
            if (auction.getStatus().equals(Auction.AuctionStatus.FINISHED)) {
                failedBidCounter.increment();
                return false;
            }
            boolean isValidBid = bidValidationService.validate(auction, bidDto);
            if (!isValidBid) {
                failedBidRedisCounter.increment();
                return false;
            }
        }
        return true;
    }

    @PostMapping("/{auctionId}/resetWinningBid")
    public ResponseEntity<Boolean> updateWinningBid(@PathVariable String auctionId) {
        log.info("Updating winning bid for auction: {}", auctionId);
        return ResponseEntity.ok(auctionService.resetWinningBid(Long.parseLong(auctionId)));
    }

    @GetMapping("/{auctionId}/reconcile")
    public ResponseEntity<String> reconcileBids(@PathVariable String auctionId) {
        return ResponseEntity.ok(String.valueOf(auctionService.reconcileBids(Long.parseLong(auctionId)).longValue()));
    }

    @DeleteMapping("/{auctionId}/bids")
    public ResponseEntity<Boolean> deleteBids(@PathVariable String auctionId) {
        log.info("Deleting all bids for auction: {}", auctionId);
        return ResponseEntity.ok(auctionService.deleteBidsByAuctionId(Long.parseLong(auctionId)));
    }


}
