package com.example.HUTECHBUS.repository;

import com.example.HUTECHBUS.model.TicketPass;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketPassRepository extends MongoRepository<TicketPass, String> {
    List<TicketPass> findByUsername(String username);
    List<TicketPass> findByUsernameAndStatus(String username, String status);
    List<TicketPass> findByUsernameAndRouteAndStatus(String username, String route, String status);
    List<TicketPass> findByStatus(String status);
}
