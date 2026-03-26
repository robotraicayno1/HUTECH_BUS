package com.example.HUTECHBUS.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Đại diện cho một xe bus trong hệ thống HUTECHBUS.
 *
 * Collection MongoDB: "Vehicles"
 */
@Data
@Document(collection = "Vehicles")
public class Vehicle {

    @Id
    private String id;

    /** Biển số xe (VD: "51B-123.45") */
    private String licensePlate;

    /** Sức chứa (VD: 45 chỗ) */
    private int capacity;

    /** Trạng thái (VD: "Hoạt động", "Bảo trì", "Dừng") */
    private String status;

    /** ID của tuyến xe đang chạy, có thể null nếu xe chưa được phân công */
    private String routeId;
}
