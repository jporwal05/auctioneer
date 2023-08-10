package com.jpswcons.auctioneer.services;

import com.jpswcons.auctioneer.data.entities.Company;
import com.jpswcons.auctioneer.data.repositories.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.jpswcons.auctioneer.utils.AuctioneerUtils.isLastMinuteBid;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    private final RedisTemplate<String, Object> redisTemplate;


    @Autowired
    public CompanyService(CompanyRepository companyRepository,
                          RedisTemplate<String, Object> redisTemplate,
                          StatusUpdaterService statusUpdaterService) {
        this.companyRepository = companyRepository;
        this.redisTemplate = redisTemplate;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public boolean increaseEndTime(long companyId, Duration duration) {
        AtomicBoolean atomicBoolean = new AtomicBoolean();
        atomicBoolean.set(false);
        Company company = companyRepository.findById(companyId).orElseThrow();
        company.getAuctions().stream().filter(auction -> isLastMinuteBid(auction.getEndTime()))
                .forEach(auction -> {
                    atomicBoolean.set(true);
            auction.setEndTime(auction.getEndTime().plus(duration));
            final String aId = String.valueOf(auction.getId());
            redisTemplate.opsForHash().put(aId, aId, auction);
        });
        return atomicBoolean.get();
    }
}
