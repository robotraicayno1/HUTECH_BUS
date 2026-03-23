package com.example.HUTECHBUS.repository;

import com.example.HUTECHBUS.model.ActiveTrip;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActiveTripRepository extends MongoRepository<ActiveTrip, String> {
    
    // Tìm các chuyến đang chạy của một tài xế cụ thể
    Optional<ActiveTrip> findByDriverIdAndStatus(String driverId, String status);
    
    // Tìm tất cả chuyến đang chạy thuộc một tuyến nhất định
    List<ActiveTrip> findByRouteIdAndStatus(String routeId, String status);
}
