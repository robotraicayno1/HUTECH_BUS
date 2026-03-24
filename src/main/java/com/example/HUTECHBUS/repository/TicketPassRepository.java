package com.example.HUTECHBUS.repository;

import com.example.HUTECHBUS.model.TicketPass;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketPassRepository extends MongoRepository<TicketPass, String> {
    List<TicketPass> findByUserId(String userId);
    Optional<TicketPass> findByUserIdAndStatus(String userId, String status);
}
