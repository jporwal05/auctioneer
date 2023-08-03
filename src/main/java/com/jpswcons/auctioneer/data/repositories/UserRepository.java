package com.jpswcons.auctioneer.data.repositories;

import com.jpswcons.auctioneer.data.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {


}
