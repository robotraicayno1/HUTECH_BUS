package com.example.HUTECHBUS.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Đại diện cho một vé đã đặt thành công của hành khách.
 * Lưu trữ lịch sử đặt vé rõ ràng và dễ dàng truy vấn từ database.
 */
@Data
@Document(collection = "Tickets")
public class Ticket {
    
    @Id
    private String id;

    private String username;        // Mã số sinh viên
    private String routeName;       // Tên tuyến xe
    private String pickupPoint;     // Điểm đón
    private String paymentMethod;   // CASH, PASS, TRANSFER
    
    private List<Integer> seats;   // Các ghế đã đặt
    private long totalAmount;       // Tổng tiền (có thể là 0đ nếu xài thẻ)
    
    private LocalDateTime bookingTime; // Thời điểm đặt giữ chỗ

    /** Trạng thái vé: BOOKED, COMPLETED, CANCELLED */
    private String status = "BOOKED";

    /** ID của chuyến xe thực tế đã hoàn thành (nếu có) */
    private String tripId;

    /** Thời điểm chuyến xe hoàn thành */
    private LocalDateTime completedAt;
}

