package com.jpswcons.auctioneer.services;

import com.jpswcons.auctioneer.data.entities.Company;
import com.jpswcons.auctioneer.data.repositories.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    private final RedisTemplate<String, Object> redisTemplate;

    private final StatusUpdaterService statusUpdaterService;


    @Autowired
    public CompanyService(CompanyRepository companyRepository,
                          RedisTemplate<String, Object> redisTemplate,
                          StatusUpdaterService statusUpdaterService) {
        this.companyRepository = companyRepository;
        this.redisTemplate = redisTemplate;
        this.statusUpdaterService = statusUpdaterService;
    }

    @Transactional
    public boolean increaseEndTime(long companyId, Duration duration) {
        Company company = companyRepository.findById(companyId).orElseThrow();
        company.getAuctions().forEach(auction -> {
            auction.setEndTime(auction.getEndTime().plus(duration));
            // update status to LIVE or FINISHED by comparing against now()
            // this is a workaround to avoid using cron or something similar
            statusUpdaterService.updateStatus(auction);
            final String aId = String.valueOf(auction.getId());
            redisTemplate.opsForHash().put(aId, aId, auction);
        });
        return true;
    }
}
