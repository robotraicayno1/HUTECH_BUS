package com.example.HUTECHBUS.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Đại diện cho một tuyến xe bus trong hệ thống HUTECHBUS.
 *
 * Collection MongoDB: "Routes"
 */
@Data
@Document(collection = "Routes")
public class Route {

    @Id
    private String id;

    /** Tên tuyến xe (VD: "Tuyến 01: Campus A - Campus E") */
    private String name;

    /** Mô tả chi tiết lộ trình */
    private String description;

    /** Danh sách các ID trạm dừng theo thứ tự lộ trình */
    private List<String> stopIds;

    /** Mã màu HEX dùng để hiển thị tuyến trên bản đồ (VD: "#ff6b00") */
    private String colorCode;
}
