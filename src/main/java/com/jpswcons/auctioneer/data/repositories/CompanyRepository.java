package com.jpswcons.auctioneer.data.repositories;

import com.jpswcons.auctioneer.data.entities.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
}
