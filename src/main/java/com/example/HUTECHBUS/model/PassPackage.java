package com.example.HUTECHBUS.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Đại diện cho một Gói Cước Thẻ Vé (tùy chỉnh bởi Admin).
 */
@Data
@Document(collection = "PassPackages")
public class PassPackage {

    @Id
    private String id;

    /** Tên hiển thị của gói (VD: Thẻ Tuần Sinh Viên) */
    private String name;

    /** Loại thời hạn: WEEK, MONTH, YEAR */
    private String type;

    /** Giá bán của gói cước (VNĐ) */
    private long price;

    /** Mô tả thêm (VD: Hiệu lực 7 ngày) */
    private String description;

    /** Thời gian hiệu lực tính bằng ngày: 7, 30, hoặc 365 */
    private int durationDays;
}
