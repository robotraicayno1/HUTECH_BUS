package com.example.HUTECHBUS.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Mô hình đại diện cho một thẻ vé định kỳ (Ticket Pass) trong hệ thống HUTECHBUS.
 * Thẻ này cho phép sinh viên đi xe bus miễn phí không giới hạn trong một khoảng thời gian.
 */
@Data
@Document(collection = "TicketPasses")
public class TicketPass {

    @Id
    private String id;

    /** 
     * Tên đăng nhập (username) hoặc ID của sinh viên sở hữu thẻ.
     * Dùng để kiểm tra quyền lợi khi đặt chỗ.
     */
    private String userId;

    /** 
     * Loại thẻ: 
     * - WEEK: Thẻ tuần (7 ngày)
     * - MONTH: Thẻ tháng (30 ngày)
     * - YEAR: Thẻ năm (365 ngày)
     */
    private String type;

    /** Ngày và giờ bắt đầu mua hoặc kích hoạt thẻ. */
    private LocalDateTime purchaseDate;

    /** 
     * Ngày và giờ hết hạn của thẻ. 
     * Sau thời điểm này, thẻ sẽ chuyển sang trạng thái EXPIRED.
     */
    private LocalDateTime expiryDate;

    /** 
     * Giá gốc của gói cước tại thời điểm mua (VNĐ).
     * Lưu lại để phục vụ báo cáo và thống kê.
     */
    private long price;

    /** 
     * Trạng thái hiện tại của thẻ:
     * - ACTIVE: Đang trong thời hạn sử dụng.
     * - EXPIRED: Đã hết hạn.
     */
    private String status = "ACTIVE";
}
