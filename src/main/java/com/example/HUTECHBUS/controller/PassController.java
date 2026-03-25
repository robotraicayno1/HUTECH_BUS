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
     * Khởi tạo quá trình mua thẻ vé định kỳ từ danh sách PassPackage.
     * Tạo URL thanh toán VNPAY hoặc kích hoạt trực tiếp nếu trả toàn bộ bằng H-Point.
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

        Integer pointsToUse = (Integer) payload.getOrDefault("pointsToUse", 0);
        if (pointsToUse < 0) return ResponseEntity.badRequest().body("Số điểm không hợp lệ.");

        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(401).body("Người dùng không tồn tại.");

        if (user.getHPoints() < pointsToUse) {
            return ResponseEntity.badRequest().body("Bạn không đủ điểm H-Point.");
        }

        long originalPrice = passPackage.getPrice();
        long discount = pointsToUse * 100L;
        
        // --- XỬ LÝ VOUCHER (MỚI) ---
        String voucherId = (String) payload.get("voucherId");
        long voucherDiscount = 0;
        if (voucherId != null && !voucherId.trim().equalsIgnoreCase("none") && !voucherId.trim().isEmpty()) {
            Optional<com.example.HUTECHBUS.model.UserVoucher> uvOpt = userVoucherRepository.findById(voucherId);
            if (uvOpt.isPresent()) {
                com.example.HUTECHBUS.model.UserVoucher uv = uvOpt.get();
                if ("ACTIVE".equals(uv.getStatus()) && uv.getUserId().equals(user.getId())) {
                    voucherDiscount = uv.getDiscountAmount();
                }
            }
        }

        long finalPrice = originalPrice - discount - voucherDiscount;
        if (finalPrice < 0) finalPrice = 0;

        String username = principal.getName();

        // 1. Thanh toán toàn bộ bằng H-Point/Voucher -> Kích hoạt luôn
        if (finalPrice <= 0) {
            if (pointsToUse > 0) {
                user.setHPoints(user.getHPoints() - pointsToUse);
                userRepository.save(user);
            }
            
            // Đánh dấu voucher đã dùng (nếu có)
            if (voucherId != null && !voucherId.trim().equalsIgnoreCase("none") && !voucherId.trim().isEmpty()) {
                userVoucherRepository.findById(voucherId).ifPresent(uv -> {
                    uv.setStatus("USED");
                    userVoucherRepository.save(uv);
                });
            }

            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.LocalDateTime[] startDateRef = { now };

            if (user.getActivePassId() != null) {
                ticketPassRepository.findById(user.getActivePassId()).ifPresent(oldPass -> {
                    if ("ACTIVE".equals(oldPass.getStatus()) && oldPass.getExpiryDate().isAfter(now)) {
                        startDateRef[0] = oldPass.getExpiryDate();
                    }
                });
            }
            java.time.LocalDateTime startDate = startDateRef[0];
            java.time.LocalDateTime expiryDate = startDate.plusDays(passPackage.getDurationDays());

            TicketPass newPass = new TicketPass();
            newPass.setUserId(username);
            newPass.setType(passPackage.getType());
            newPass.setPrice(passPackage.getPrice());
            newPass.setPurchaseDate(now);
            newPass.setExpiryDate(expiryDate);
            newPass.setStatus("ACTIVE");

            TicketPass savedPass = ticketPassRepository.save(newPass);
            user.setActivePassId(savedPass.getId());
            userRepository.save(user);

            return ResponseEntity.ok(Map.of("message", "Kích hoạt chức năng thẻ thành công!", "redirect", "/my-tickets"));
        }

        // 2. Còn số tiền thực trả -> Đi qua VNPAY
        String orderInfo = "PASS:" + packageId + ":" + username + ":" + pointsToUse + ":" + (voucherId != null ? voucherId : "NONE");
        
        String paymentUrl = vnPayService.createPaymentUrl(request, finalPrice, orderInfo);
        return ResponseEntity.ok(Map.of("message", "Redirecting to VNPAY", "paymentUrl", paymentUrl));
    }
}
