package com.example.HUTECHBUS.controller;

import com.example.HUTECHBUS.config.VnPayConfig;
import com.example.HUTECHBUS.model.TicketPass;
import com.example.HUTECHBUS.model.User;
import com.example.HUTECHBUS.repository.TicketPassRepository;
import com.example.HUTECHBUS.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private TicketPassRepository ticketPassRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/create-payment")
    public ResponseEntity<?> createPayment(HttpServletRequest req, @RequestBody Map<String, Object> payload) throws UnsupportedEncodingException {
        long amount = Long.parseLong(payload.get("amount").toString()) * 100;
        String vnp_TxnRef = VnPayConfig.getRandomNumber(8);
        String vnp_IpAddr = VnPayConfig.getIpAddress(req);
        String vnp_TmnCode = VnPayConfig.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", VnPayConfig.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));
        
        cld.add(Calendar.MINUTE, 15);
        vnp_Params.put("vnp_ExpireDate", formatter.format(cld.getTime()));

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                
                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = VnPayConfig.hmacSHA512(VnPayConfig.secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = VnPayConfig.vnp_PayUrl + "?" + queryUrl;

        Map<String, Object> result = new HashMap<>();
        result.put("code", "00");
        result.put("message", "success");
        result.put("data", paymentUrl);
        return ResponseEntity.ok(result);
    }

    /**
     * Webhook/Callback từ VNPAY sau khi người dùng thanh toán xong.
     * VNPAY sẽ gọi API này (hoặc người dùng bị redirect về đây).
     */
    @GetMapping("/vnpay-payment-return")
    public void vnpayPaymentReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String responseCode = request.getParameter("vnp_ResponseCode");
        String orderInfo = request.getParameter("vnp_OrderInfo");

        // Code "00" có nghĩa là thanh toán thành công
        if ("00".equals(responseCode)) {
            // Kiểm tra xem đây có phải là giao dịch mua Thẻ Vé Định Kỳ không (dựa vào tiền tố PASS:)
            if (orderInfo != null && orderInfo.startsWith("PASS:")) {
                // Xử lý logic mua/gia hạn thẻ vé
                String[] parts = orderInfo.split(":");
                // Cấu trúc dự kiến: PASS:{LoạiThẻ}:{Username}:{PointsToUse}
                if (parts.length >= 3) {
                    String type = parts[1]; // WEEK, MONTH, YEAR
                    String username = parts[2];
                    int pointsUsed = 0;
                    if (parts.length >= 4) {
                        try {
                            pointsUsed = Integer.parseInt(parts[3]);
                        } catch (NumberFormatException ignored) {}
                    }
                    
                    Optional<User> userOpt = userRepository.findByUsername(username);
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();

                        // Trừ điểm nếu có sử dụng
                        if (pointsUsed > 0) {
                            user.setHPoints(user.getHPoints() - pointsUsed);
                        }
                        
                        LocalDateTime now = LocalDateTime.now();
                        LocalDateTime startDate = now;

                        // -- LOGIC GIA HẠN KHI THẺ CŨ CÒN HẠN (CHAINING) --
                        if (user.getActivePassId() != null) {
                            Optional<TicketPass> oldPassOpt = ticketPassRepository.findById(user.getActivePassId());
                            if (oldPassOpt.isPresent() && "ACTIVE".equals(oldPassOpt.get().getStatus())) {
                                if (oldPassOpt.get().getExpiryDate().isAfter(now)) {
                                    startDate = oldPassOpt.get().getExpiryDate();
                                }
                            }
                        }

                        // Tính toán thời gian hết hạn mới dựa trên điểm bắt đầu (startDate)
                        LocalDateTime expiryDate;
                        switch (type.toUpperCase()) {
                            case "WEEK": expiryDate = startDate.plusDays(7); break;
                            case "MONTH": expiryDate = startDate.plusMonths(1); break;
                            case "YEAR": expiryDate = startDate.plusYears(1); break;
                            default: expiryDate = startDate.plusMonths(1);
                        }

                        // Tạo thẻ mới trong CSDL
                        TicketPass newPass = new TicketPass();
                        newPass.setUserId(username);
                        newPass.setType(type.toUpperCase());
                        newPass.setPurchaseDate(now);
                        newPass.setExpiryDate(expiryDate);
                        newPass.setStatus("ACTIVE");

                        TicketPass savedPass = ticketPassRepository.save(newPass);
                        
                        // Cập nhật lại User
                        user.setActivePassId(savedPass.getId());
                        userRepository.save(user);
                    }
                }
            }
            // Chuyển hướng người dùng về Dashboard kèm cờ thành công
            response.sendRedirect("/dashboard?vnpay_status=success");
        } else {
            // Thanh toán thất bại hoặc bị hủy
            response.sendRedirect("/dashboard?vnpay_status=error");
        }
    }
}
