package com.example.HUTECHBUS.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Thông báo gửi tới người dùng (VD: chuyến xe kết thúc).
 *
 * Collection MongoDB: "Notifications"
 */
@Data
@Document(collection = "Notifications")
public class Notification {

    @Id
    private String id;

    /** Username người nhận thông báo */
    private String userId;

    /** Tiêu đề thông báo */
    private String title;

    /** Nội dung chi tiết */
    private String message;

    /** Loại thông báo: TRIP_COMPLETE, SYSTEM, etc. */
    private String type;

    /** Thời điểm tạo */
    private LocalDateTime createdAt = LocalDateTime.now();

    /** Đã đọc chưa */
    private boolean read = false;
}
