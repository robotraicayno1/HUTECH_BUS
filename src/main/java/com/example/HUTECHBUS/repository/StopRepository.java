package com.example.HUTECHBUS.repository;

import com.example.HUTECHBUS.model.Stop;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository thao tác với collection "Stops" trong MongoDB.
 */
public interface StopRepository extends MongoRepository<Stop, String> {
}
