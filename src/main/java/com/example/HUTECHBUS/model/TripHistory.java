package com.example.HUTECHBUS.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Lưu trữ lịch sử chuyến đi của một người dùng.
 *
 * Collection MongoDB: "TripHistories"
 */
@Data
@Document(collection = "TripHistories")
public class TripHistory {

    @Id
    private String id;

    /** ID của người dùng thực hiện chuyến đi */
    private String userId;

    /** ID tuyến xe đã sử dụng */
    private String routeId;

    /** Tên tuyến xe lúc ghi nhận (để hiển thị ngay cả khi tuyến bị xóa) */
    private String routeName;

    /** Thời điểm thực hiện chuyến đi */
    private LocalDateTime tripDate;

    /** Trạng thái chuyến đi: COMPLETED hoặc CANCELLED */
    private String status;
}
