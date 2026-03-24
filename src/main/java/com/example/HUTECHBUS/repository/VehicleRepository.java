package com.example.HUTECHBUS.repository;

import com.example.HUTECHBUS.model.Vehicle;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends MongoRepository<Vehicle, String> {
    List<Vehicle> findByRouteId(String routeId);
}
