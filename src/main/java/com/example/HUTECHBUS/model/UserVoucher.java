package com.example.HUTECHBUS.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Đại diện cho Voucher mà người dùng đã đổi.
 *
 * Collection MongoDB: "UserVouchers"
 */
@Data
@Document(collection = "UserVouchers")
public class UserVoucher {

    @Id
    private String id;

    /** ID của người dùng sở hữu voucher này */
    private String userId;

    /** ID của voucher gốc */
    private String voucherId;

    /** Lưu tên của voucher tại thời điểm đổi */
    private String voucherName;

    /** Loại vé (DAILY, MONTHLY, YEARLY) */
    private String ticketType;

    /** Số tiền giảm giá */
    private int discountAmount;

    /** Trạng thái voucher: ACTIVE, USED, EXPIRED */
    private String status;

    /** Thời điểm người dùng đổi voucher này */
    private LocalDateTime acquiredDate;
}
