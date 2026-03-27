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
import com.example.HUTECHBUS.model.PassPackage;
import com.example.HUTECHBUS.repository.PassPackageRepository;
import com.example.HUTECHBUS.repository.UserVoucherRepository;
import com.example.HUTECHBUS.model.UserVoucher;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

/**
 * Controller xử lý các nghiệp vụ liên quan đến Thẻ Vé Định Kỳ (Ticket Pass).
 */
@Controller
@RequestMapping("/api/passes")
public class PassController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketPassRepository ticketPassRepository;

    @Autowired
    private PassPackageRepository passPackageRepository;

    @Autowired
    private UserVoucherRepository userVoucherRepository;

    @Autowired
    private VnPayService vnPayService;

    /**
     * Xử lý yêu cầu mua Thẻ Vé Định Kỳ (Ticket Pass).
     * 
     * Quy trình:
     * 1. Kiểm tra đăng nhập và tính hợp lệ của gói cước (Package).
     * 2. Tính toán giảm giá từ H-Point (1 điểm = 100đ).
     * 3. Kiểm tra và áp dụng Voucher giảm giá (nếu có).
     * 4. Nếu số tiền sau giảm = 0: 
     *    - Trừ điểm, đánh dấu voucher đã dùng.
     *    - Tính toán ngày bắt đầu: Nếu đang có thẻ cũ còn hạn -> Cộng dồn từ ngày hết hạn thẻ cũ.
     *    - Kích hoạt thẻ mới ngay lập tức.
     * 5. Nếu còn tiền phải trả:
     *    - Tạo thông tin đơn hàng đầy đủ (bao gồm voucherId để xử lý sau khi thanh toán).
     *    - Trả về URL thanh toán VNPAY.
     */
    @PostMapping("/buy")
    @ResponseBody
    public ResponseEntity<?> buyPass(@RequestBody Map<String, Object> payload, 
                                   HttpServletRequest request,
                                   Principal principal) throws Exception {
        if (principal == null) return ResponseEntity.status(401).body("Yêu cầu đăng nhập.");
        
        String packageId = (String) payload.get("packageId");
        if (packageId == null || packageId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Vui lòng chọn một gói cước.");
        }

        PassPackage passPackage = passPackageRepository.findById(packageId).orElse(null);
        if (passPackage == null) {
            return ResponseEntity.badRequest().body("Gói cước không tồn tại.");
        }

        // Điểm H-Point sinh viên muốn dùng
        Integer pointsToUse = (Integer) payload.getOrDefault("pointsToUse", 0);
        if (pointsToUse < 0) return ResponseEntity.badRequest().body("Số điểm không hợp lệ.");

        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(401).body("Người dùng không tồn tại.");

        if (user.getHPoints() < pointsToUse) {
            return ResponseEntity.badRequest().body("Bạn không đủ điểm H-Point.");
        }

        long originalPrice = passPackage.getPrice();
        long pointDiscount = pointsToUse * 100L;
        
        // --- XỬ LÝ VOUCHER ---
        String voucherId = (String) payload.get("voucherId");
        long voucherDiscount = 0;
        if (voucherId != null && !voucherId.trim().equalsIgnoreCase("none") && !voucherId.trim().isEmpty()) {
            Optional<com.example.HUTECHBUS.model.UserVoucher> uvOpt = userVoucherRepository.findById(voucherId);
            if (uvOpt.isPresent()) {
                com.example.HUTECHBUS.model.UserVoucher uv = uvOpt.get();
                // Chỉ áp dụng nếu voucher còn hiệu lực và thuộc về đúng user
                if ("ACTIVE".equals(uv.getStatus()) && uv.getUserId().equals(user.getId())) {
                    voucherDiscount = uv.getDiscountAmount();
                }
            }
        }

        long finalPrice = originalPrice - pointDiscount - voucherDiscount;
        if (finalPrice < 0) finalPrice = 0;

        String username = principal.getName();

        // TRƯỜNG HỢP 1: THANH TOÁN 0Đ (ĐIỂM + VOUCHER ĐÃ BAO PHỦ TOÀN BỘ)
        if (finalPrice <= 0) {
            // Trừ điểm H-Point
            if (pointsToUse > 0) {
                user.setHPoints(user.getHPoints() - pointsToUse);
                userRepository.save(user);
            }
            
            // Đánh dấu Voucher là đã sử dụng
            if (voucherId != null && !voucherId.trim().equalsIgnoreCase("none") && !voucherId.trim().isEmpty()) {
                userVoucherRepository.findById(voucherId).ifPresent(uv -> {
                    uv.setStatus("USED");
                    userVoucherRepository.save(uv);
                });
            }

            // Logic CỘNG DỒN THỜI HẠN (Stacking)
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.LocalDateTime[] startDateRef = { now };

            // Nếu user đã có thẻ đang hoạt động -> Ngày bắt đầu thẻ mới = Ngày hết hạn thẻ cũ
            if (user.getActivePassId() != null) {
                ticketPassRepository.findById(user.getActivePassId()).ifPresent(oldPass -> {
                    if ("ACTIVE".equals(oldPass.getStatus()) && oldPass.getExpiryDate().isAfter(now)) {
                        startDateRef[0] = oldPass.getExpiryDate();
                    }
                });
            }
            java.time.LocalDateTime startDate = startDateRef[0];
            java.time.LocalDateTime expiryDate = startDate.plusDays(passPackage.getDurationDays());

            // Tạo thẻ mới
            TicketPass newPass = new TicketPass();
            newPass.setUserId(username);
            newPass.setType(passPackage.getType());
            newPass.setPrice(passPackage.getPrice());
            newPass.setPurchaseDate(now);
            newPass.setExpiryDate(expiryDate);
            newPass.setStatus("ACTIVE");

            TicketPass savedPass = ticketPassRepository.save(newPass);
            
            // Cập nhật thẻ hoạt động mới nhất cho User
            user.setActivePassId(savedPass.getId());
            userRepository.save(user);

            return ResponseEntity.ok(Map.of("message", "Kích hoạt chức năng thẻ thành công!", "redirect", "/my-tickets"));
        }

        // TRƯỜNG HỢP 2: CẦN THANH TOÁN THÊM QUA VNPAY
        // Lưu packageId, username, pointsToUse, voucherId vào OrderInfo để PaymentController xử lý khi callback thành công
        String orderInfo = "PASS:" + packageId + ":" + username + ":" + pointsToUse + ":" + (voucherId != null ? voucherId : "NONE");
        
        String paymentUrl = vnPayService.createPaymentUrl(request, finalPrice, orderInfo);
        return ResponseEntity.ok(Map.of("message", "Redirecting to VNPAY", "paymentUrl", paymentUrl));
    }
}
