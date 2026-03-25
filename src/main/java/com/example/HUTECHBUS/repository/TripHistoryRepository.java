package com.example.HUTECHBUS.repository;

import com.example.HUTECHBUS.model.TripHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository thao tác với collection "TripHistories" trong MongoDB.
 */
public interface TripHistoryRepository extends MongoRepository<TripHistory, String> {

    /**
     * Lấy lịch sử chuyến đi của một người dùng, sắp xếp từ mới nhất đến cũ nhất.
     *
     * @param userId ID của người dùng
     * @return Danh sách các chuyến đi theo thứ tự giảm dần của ngày
     */
    List<TripHistory> findByUserIdOrderByTripDateDesc(String userId);

    /**
     * Lấy tất cả lịch sử chuyến đi từ một thời điểm nhất định (cho báo cáo).
     */
    List<TripHistory> findByTripDateAfter(LocalDateTime date);
}
