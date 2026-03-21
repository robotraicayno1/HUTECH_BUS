package com.example.HUTECHBUS.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Đại diện cho một chuyến xe đang chạy thực tế trên đường.
 * Chứa thông tin tài xế và trạng thái của các ghế ngồi.
 *
 * Collection MongoDB: "ActiveTrips"
 */
@Data
@Document(collection = "ActiveTrips")
public class ActiveTrip {

    @Id
    private String id;

    /** ID của tuyến xe đang chạy */
    private String routeId;

    /** Tên tuyến xe */
    private String routeName;

    /** ID của tài xế/lơ xe phụ trách chuyến này */
    private String driverId;

    /** Tổng số ghế trên xe (VD: 45) */
    private int totalSeats = 45;

    /** Danh sách các số ghế đã bị khóa bởi lơ xe (khách vãng lai, ghế hỏng...) */
    private List<Integer> lockedSeats = new ArrayList<>();

    /** Lưu ánh xạ username của sinh viên -> số ghế đã được cấp */
    private Map<String, Integer> passengerSeats = new HashMap<>();

    /** Thời điểm bắt đầu chuyến đi */
    private LocalDateTime startTime;

    /** Trạng thái chuyến đi: RUNNING (đang chạy), COMPLETED (kết thúc) */
    private String status = "RUNNING";
}
