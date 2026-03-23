package com.example.HUTECHBUS.repository;

import com.example.HUTECHBUS.model.Ticket;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends MongoRepository<Ticket, String> {
    List<Ticket> findByUsername(String username);
    List<Ticket> findByStatus(String status);
    List<Ticket> findByRouteAndDateAndTimeAndStatus(String route, String date, String time, String status);
    List<Ticket> findByRouteAndDateAndTime(String route, String date, String time);
}
