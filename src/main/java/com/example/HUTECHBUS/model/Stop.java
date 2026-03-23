package com.example.HUTECHBUS.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Đại diện cho một trạm dừng xe bus.
 *
 * Collection MongoDB: "Stops"
 */
@Data
@Document(collection = "Stops")
public class Stop {

    @Id
    private String id;

    /** Tên trạm dừng (VD: "HUTECH Campus A") */
    private String name;

    /** Vĩ độ địa lý (latitude) */
    private double latitude;

    /** Kinh độ địa lý (longitude) */
    private double longitude;

    /** Mô tả bổ sung cho trạm dừng */
    private String description;
}
