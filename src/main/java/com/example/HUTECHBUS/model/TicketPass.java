package com.example.HUTECHBUS.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Đại diện cho một thẻ vé định kỳ (Tuần, Tháng, Năm).
 */
@Data
@Document(collection = "TicketPasses")
public class TicketPass {

    @Id
    private String id;

    /** ID của người dùng sở hữu thẻ */
    private String userId;

    /** Loại thẻ: WEEK, MONTH, YEAR */
    private String type;

    /** Ngày mua thẻ */
    private LocalDateTime purchaseDate;

    /** Ngày hết hạn thẻ */
    private LocalDateTime expiryDate;

    /** Giá tiền của thẻ (VNĐ) */
    private long price;

    /** Trạng thái thẻ: ACTIVE, EXPIRED */
    private String status = "ACTIVE";
}
