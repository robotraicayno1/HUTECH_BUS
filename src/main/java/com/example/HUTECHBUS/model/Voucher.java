package com.example.HUTECHBUS.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Đại diện cho một Voucher giảm giá vé (Ngày, Tháng, Năm).
 *
 * Collection MongoDB: "Vouchers"
 */
@Data
@Document(collection = "Vouchers")
public class Voucher {

    @Id
    private String id;

    /** Tên voucher (Ví dụ: Giảm giá vé tháng 10K) */
    private String name;

    /** Loại vé giảm giá: DAILY, MONTHLY, YEARLY */
    private String ticketType;

    /** Số luợng điểm cần để đổi */
    private int pointCost;

    /** Số tiền được giảm giá khi áp dụng voucher này (VD: 10000) */
    private int discountAmount;
}
