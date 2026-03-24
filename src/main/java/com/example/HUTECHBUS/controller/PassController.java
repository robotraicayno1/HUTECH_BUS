package com.example.HUTECHBUS.controller;

import com.example.HUTECHBUS.service.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@Controller
@RequestMapping("/api/passes")
public class PassController {

    @Autowired
    private VnPayService vnPayService;

    /**
     * Khởi tạo quá trình mua thẻ vé định kỳ.
     * Tạo URL thanh toán VNPAY và trả về cho client để redirect.
     * 
     * @param payload Chứa loại thẻ (WEEK, MONTH, YEAR)
     */
    @PostMapping("/buy")
    @ResponseBody
    public ResponseEntity<?> buyPass(@RequestBody Map<String, String> payload, 
                                   HttpServletRequest request,
                                   Principal principal) throws Exception {
        // Kiểm tra đăng nhập
        if (principal == null) return ResponseEntity.status(401).body("Yêu cầu đăng nhập.");
        
        String type = payload.get("type"); // WEEK, MONTH, YEAR
        if (type == null) return ResponseEntity.badRequest().body("Loại thẻ không hợp lệ.");

        // Xác định giá tiền dựa trên loại thẻ
        long price;
        switch (type.toUpperCase()) {
            case "WEEK": price = 50000; break;
            case "MONTH": price = 180000; break;
            case "YEAR": price = 1500000; break;
            default: return ResponseEntity.badRequest().body("Loại thẻ không hỗ trợ.");
        }

        // Tạo orderInfo có tiền tố PASS: để phân biệt với thanh toán vé lẻ thường trong PaymentController
        String orderInfo = "PASS:" + type.toUpperCase() + ":" + principal.getName();
        
        // Gọi service tạo URL thanh toán bảo mật của VNPAY
        String paymentUrl = vnPayService.createPaymentUrl(request, price, orderInfo);

        // Trả URL về cho Frontend xử lý redirect
        return ResponseEntity.ok(Map.of("message", "Redirecting to VNPAY", "paymentUrl", paymentUrl));
    }
}
