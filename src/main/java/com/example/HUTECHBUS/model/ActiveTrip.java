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

    /** Danh sách các số ghế đã khóa Tiền mặt (bởi lơ xe) */
    private List<Integer> lockedSeats = new ArrayList<>();

    /** Danh sách các số ghế đã khóa Chuyển khoản (bởi lơ xe) */
    private List<Integer> transferPaidSeats = new ArrayList<>();

    /** Lưu ánh xạ username -> danh sách các số ghế đã đặt online */
    private Map<String, List<Integer>> passengerSeats = new HashMap<>();

    /** Danh sách các ghế đặt online bằng Tiền mặt (chưa trả tiền) */
    private List<Integer> onlineUnpaidSeats = new ArrayList<>();

    /** Danh sách các ghế đặt online đã trả tiền (VNPAY/Transfer) */
    private List<Integer> onlinePaidSeats = new ArrayList<>();

    /** Danh sách các ghế đã được tài xế xác nhận lên xe qua QR */
    private List<Integer> checkedInSeats = new ArrayList<>();

    /** Thời điểm bắt đầu chuyến đi */
    private LocalDateTime startTime;

    /** Trạng thái chuyến đi: RUNNING (đang chạy), COMPLETED (kết thúc) */
    private String status = "RUNNING";
}
