package com.example.HUTECHBUS.repository;

import com.example.HUTECHBUS.model.TripHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository thao tác với collection "TripHistories" trong MongoDB.
 */
public interface TripHistoryRepository extends MongoRepository<TripHistory, String> {

    List<TripHistory> findByUserIdOrderByTripDateDesc(String userId);

    List<TripHistory> findByTripDateAfter(LocalDateTime date);

    List<TripHistory> findByRouteId(String routeId);

    List<TripHistory> findByTripDateBetween(LocalDateTime start, LocalDateTime end);

    long countByRouteId(String routeId);
}
