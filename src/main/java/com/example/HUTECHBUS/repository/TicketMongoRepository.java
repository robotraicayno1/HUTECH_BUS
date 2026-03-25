package com.example.HUTECHBUS.repository;

import com.example.HUTECHBUS.model.Ticket;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketMongoRepository extends MongoRepository<Ticket, String> {
    List<Ticket> findByUsernameOrderByBookingTimeDesc(String username);
    List<Ticket> findByUsernameAndStatus(String username, String status);
    List<Ticket> findByUsernameAndTripId(String username, String tripId);
}

