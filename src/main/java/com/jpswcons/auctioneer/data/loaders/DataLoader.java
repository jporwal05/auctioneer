package com.jpswcons.auctioneer.data.loaders;

import com.jpswcons.auctioneer.data.entities.Auction;
import com.jpswcons.auctioneer.data.entities.Company;
import com.jpswcons.auctioneer.data.repositories.AuctionRepository;
import com.jpswcons.auctioneer.data.repositories.CompanyRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Log4j2
public class DataLoader implements CommandLineRunner {


    private final CompanyRepository companyRepository;
    private final AuctionRepository auctionRepository;

    @Autowired
    public DataLoader(CompanyRepository companyRepository, AuctionRepository auctionRepository) {
        this.companyRepository = companyRepository;
        this.auctionRepository = auctionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Loading company and auction data");

        List<Company> companies = List.of(Company.builder().name("Apple").build(),
                Company.builder().name("Google").build(),
                Company.builder().name("Microsoft").build());
        List<Company> savedCompanies = companyRepository.saveAll(companies);

        List<Auction> auctions = List.of(Auction.builder()
                        .company(savedCompanies.get(0))
                        .itemName("IC-255")
                        .startTime(LocalDateTime.now())
                        .endTime(LocalDateTime.now().plusMinutes(5))
                        .status(Auction.AuctionStatus.LIVE)
                        .startingPrice(150000)
                        .stepPrice(100).build(),
                Auction.builder()
                        .company(savedCompanies.get(0))
                        .itemName("IC-355")
                        .startTime(LocalDateTime.now())
                        .endTime(LocalDateTime.now().plusMinutes(5))
                        .status(Auction.AuctionStatus.LIVE)
                        .startingPrice(200000)
                        .stepPrice(100).build()
                );
        auctionRepository.saveAll(auctions);
        log.info("Loaded company and auction data");
    }
}
