package com.example.HUTECHBUS.repository;

import com.example.HUTECHBUS.model.Route;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository thao tác với collection "Routes" trong MongoDB.
 */
public interface RouteRepository extends MongoRepository<Route, String> {
}
