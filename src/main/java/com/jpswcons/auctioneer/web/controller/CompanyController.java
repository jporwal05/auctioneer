package com.jpswcons.auctioneer.web.controller;

import com.jpswcons.auctioneer.data.entities.Company;
import com.jpswcons.auctioneer.data.repositories.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1/companies")
public class CompanyController {

    private final CompanyRepository companyRepository;

    @Autowired
    public CompanyController(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @GetMapping
    public ResponseEntity<List<Company>> getCompanies() {
        List<Company> companies = companyRepository.findAll();
        return ResponseEntity.of(Optional.of(companies));
    }
}
