package com.example.HUTECHBUS.controller;

import com.example.HUTECHBUS.service.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.example.HUTECHBUS.model.User;
import com.example.HUTECHBUS.repository.UserRepository;
import com.example.HUTECHBUS.model.TicketPass;
import com.example.HUTECHBUS.repository.TicketPassRepository;
import java.security.Principal;
import java.util.Map;

@Controller
@RequestMapping("/api/passes")
public class PassController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketPassRepository ticketPassRepository;

    @Autowired
    private VnPayService vnPayService;

    /**
     * Khởi tạo quá trình mua thẻ vé định kỳ.
     * Tạo URL thanh toán VNPAY hoặc kích hoạt trực tiếp nếu đủ điểm.
     */
    @PostMapping("/buy")
    @ResponseBody
    public ResponseEntity<?> buyPass(@RequestBody Map<String, Object> payload, 
                                   HttpServletRequest request,
                                   Principal principal) throws Exception {
        // Kiểm tra đăng nhập
        if (principal == null) return ResponseEntity.status(401).body("Yêu cầu đăng nhập.");
        
        String type = (String) payload.get("type"); // WEEK, MONTH, YEAR
        if (type == null) return ResponseEntity.badRequest().body("Loại thẻ không hợp lệ.");

        Integer pointsToUse = (Integer) payload.getOrDefault("pointsToUse", 0);
        if (pointsToUse < 0) return ResponseEntity.badRequest().body("Số điểm không hợp lệ.");

        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(401).body("Người dùng không tồn tại.");

        if (user.getHPoints() < pointsToUse) {
            return ResponseEntity.badRequest().body("Bạn không đủ điểm H-Point.");
        }

        // Xác định giá tiền dựa trên loại thẻ
        long originalPrice;
        switch (type.toUpperCase()) {
            case "WEEK": originalPrice = 50000; break;
            case "MONTH": originalPrice = 180000; break;
            case "YEAR": originalPrice = 1500000; break;
            default: return ResponseEntity.badRequest().body("Loại thẻ không hỗ trợ.");
        }

        // 1 điểm = 100 VNĐ
        long discount = pointsToUse * 100L;
        long finalPrice = originalPrice - discount;
        if (finalPrice < 0) finalPrice = 0;

        String username = principal.getName();

        // Trường hợp 1: Thanh toán toàn bộ bằng điểm (0 VNĐ) -> Kích hoạt luôn
        if (finalPrice <= 0) {
            user.setHPoints(user.getHPoints() - pointsToUse);
            userRepository.save(user);

            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            // Dùng mảng để tránh lỗi "effectively final" trong lambda
            java.time.LocalDateTime[] startDateRef = { now };

            // Xử lý gia hạn nối tiếp nếu đang có thẻ còn hạn
            if (user.getActivePassId() != null) {
                ticketPassRepository.findById(user.getActivePassId()).ifPresent(oldPass -> {
                    if ("ACTIVE".equals(oldPass.getStatus()) && oldPass.getExpiryDate().isAfter(now)) {
                        startDateRef[0] = oldPass.getExpiryDate();
                    }
                });
            }
            java.time.LocalDateTime startDate = startDateRef[0];

            java.time.LocalDateTime expiryDate;
            switch (type.toUpperCase()) {
                case "WEEK": expiryDate = startDate.plusDays(7); break;
                case "MONTH": expiryDate = startDate.plusMonths(1); break;
                case "YEAR": expiryDate = startDate.plusYears(1); break;
                default: expiryDate = startDate.plusMonths(1);
            }

            TicketPass newPass = new TicketPass();
            newPass.setUserId(username);
            newPass.setType(type.toUpperCase());
            newPass.setPurchaseDate(now);
            newPass.setExpiryDate(expiryDate);
            newPass.setStatus("ACTIVE");

            TicketPass savedPass = ticketPassRepository.save(newPass);
            user.setActivePassId(savedPass.getId());
            userRepository.save(user);

            return ResponseEntity.ok(Map.of("message", "Kích hoạt thẻ thành công bằng điểm!", "redirect", "/dashboard?vnpay_status=success"));
        }

        // Trường hợp 2: Còn số thực trả -> Đi qua VNPAY
        // Thêm format pointsToUse vào orderInfo để PaymentController xử lý trừ điểm sau khi pay thành công
        String orderInfo = "PASS:" + type.toUpperCase() + ":" + username + ":" + pointsToUse;
        
        String paymentUrl = vnPayService.createPaymentUrl(request, finalPrice, orderInfo);
        return ResponseEntity.ok(Map.of("message", "Redirecting to VNPAY", "paymentUrl", paymentUrl));
    }
}
