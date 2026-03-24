package com.example.HUTECHBUS.controller;

import com.example.HUTECHBUS.model.ActiveTrip;
import com.example.HUTECHBUS.model.TicketPass;
import com.example.HUTECHBUS.model.Ticket;
import com.example.HUTECHBUS.model.User;
import com.example.HUTECHBUS.repository.ActiveTripMongoRepository;
import com.example.HUTECHBUS.repository.TicketPassRepository;
import com.example.HUTECHBUS.repository.TicketMongoRepository;
import com.example.HUTECHBUS.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Quản lý các luồng liên quan đến việc hiển thị trang đặt vé,
 * danh sách vé cá nhân, chi tiết vé điện tử, màn hình mua thẻ vé,
 * và các API tương tác trực tiếp với quá trình chọn ghế/giữ chỗ của chuyến xe đang chạy.
 */
@Controller
public class BookingController {
    
    @Autowired
    private ActiveTripMongoRepository activeTripRepository;

    @Autowired
    private TicketPassRepository ticketPassRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketMongoRepository ticketMongoRepository;

    @GetMapping("/booking")
    public String showBookingPage() {
        return "booking";
    }

    /**
     * Giao diện Xem chi tiết vé điện tử / Boarding Pass.
     * Cung cấp thông tin User và Thẻ vé đang hoạt động (nếu có)
     * xuống Model để Frontend render Mã QR tương ứng.
     */
    @GetMapping("/ticket")
    public String showTicketPage(Model model, Authentication authentication) {
        if (authentication != null) {
            String username = authentication.getName();
            model.addAttribute("username", username);
            userRepository.findByUsername(username).ifPresent(user -> {
                model.addAttribute("user", user);
                if (user.getActivePassId() != null) {
                    ticketPassRepository.findById(user.getActivePassId()).ifPresent(pass -> {
                        model.addAttribute("activePass", pass);
                    });
                }
            });
        }
        return "ticket";
    }

    /**
     * Giao diện Xem danh sách lịch sử vé và mã QR của người dùng.
     * Gắn sẵn thông tin User và thẻ thành viên vào giao diện.
     */
    @GetMapping("/my-tickets")
    public String showMyTicketsPage(Model model, Authentication authentication) {
        if (authentication != null) {
            String username = authentication.getName();
            model.addAttribute("username", username);
            userRepository.findByUsername(username).ifPresent(user -> {
                model.addAttribute("user", user);
                if (user.getActivePassId() != null) {
                    ticketPassRepository.findById(user.getActivePassId()).ifPresent(pass -> {
                        model.addAttribute("activePass", pass);
                    });
                }
            });
        }
        return "my-tickets";
    }

    /**
     * Giao diện chọn mua các gói Thẻ Vé Định Kỳ (Tuần/Tháng/Năm).
     */
    @GetMapping("/buy-pass")
    public String showBuyPassPage(Model model, Principal principal) {
        if (principal != null) {
            userRepository.findByUsername(principal.getName()).ifPresent(user -> {
                model.addAttribute("hPoints", user.getHPoints());
            });
        }
        return "buy-pass";
    }

    // --- CÁC API ĐỒNG BỘ VỚI DRIVER ---

    @GetMapping("/api/users/me/active-pass")
    @ResponseBody
    public ResponseEntity<?> getActivePass(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body("Yêu cầu đăng nhập.");
        
        Optional<User> userOpt = userRepository.findByUsername(principal.getName());
        if (userOpt.isEmpty() || userOpt.get().getActivePassId() == null) {
            return ResponseEntity.ok(Map.of("hasActivePass", false));
        }

        Optional<TicketPass> passOpt = ticketPassRepository.findById(userOpt.get().getActivePassId());
        if (passOpt.isPresent() && "ACTIVE".equals(passOpt.get().getStatus())) {
            return ResponseEntity.ok(Map.of("hasActivePass", true, "pass", passOpt.get()));
        }
        
        return ResponseEntity.ok(Map.of("hasActivePass", false));
    }

    /**
     * Lấy danh sách lịch sử vé đã đặt của tôi.
     */
    @GetMapping("/api/tickets/me")
    @ResponseBody
    public ResponseEntity<?> getMyTickets(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body("Yêu cầu đăng nhập.");
        return ResponseEntity.ok(ticketMongoRepository.findByUsernameOrderByBookingTimeDesc(principal.getName()));
    }

    /**
     * Lấy thông tin chuyến xe đang chạy cho một tuyến đường cụ thể.
     */
    @GetMapping("/api/bookings/active/{routeId}")
    @ResponseBody
    public ResponseEntity<?> getActiveTripForRoute(@PathVariable String routeId) {
        Optional<ActiveTrip> trip = activeTripRepository.findByRouteIdAndStatus(routeId, "RUNNING").stream().findFirst();
        return trip.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Đặt chỗ và khóa ghế trên chuyến xe đang chạy.
     */
    @PostMapping("/api/bookings/reserve")
    @ResponseBody
    public ResponseEntity<?> reserveSeats(@RequestBody Map<String, Object> payload, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body("Yêu cầu đăng nhập.");
        
        String routeId = (String) payload.get("routeId");
        String paymentType = (String) payload.getOrDefault("paymentType", "TRANSFER");
        @SuppressWarnings("unchecked")
        List<Integer> seatNumbers = (List<Integer>) payload.get("seatNumbers");
        String username = principal.getName();

        Optional<ActiveTrip> tripOpt = activeTripRepository.findByRouteIdAndStatus(routeId, "RUNNING").stream().findFirst();
        if (tripOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Hiện không có chuyến xe nào đang chạy cho tuyến này.");
        }

        ActiveTrip trip = tripOpt.get();
        List<Integer> locked = trip.getLockedSeats();
        List<Integer> tPaid = trip.getTransferPaidSeats();
        List<Integer> pPaid = trip.getOnlinePaidSeats();
        List<Integer> pUnpaid = trip.getOnlineUnpaidSeats();
        Map<String, List<Integer>> pSeats = trip.getPassengerSeats();

        // Kiểm tra xem ghế đã bị khóa bởi bất kỳ danh sách nào chưa
        for (Integer seat : seatNumbers) {
            if (locked.contains(seat) || tPaid.contains(seat) || pPaid.contains(seat) || pUnpaid.contains(seat)) {
                return ResponseEntity.badRequest().body("Ghế " + seat + " đã có người đặt hoặc bị khóa.");
            }
        }

        // Cập nhật ghế của sinh viên
        List<Integer> userSeats = pSeats.getOrDefault(username, new ArrayList<>());
        userSeats.addAll(seatNumbers);
        pSeats.put(username, userSeats);
        
        // Cập nhật điểm đón nếu có
        String pickupPoint = (String) payload.get("pickupPoint");
        if (pickupPoint != null && !pickupPoint.isEmpty()) {
            Map<String, String> pPickups = trip.getPassengerPickupPoints();
            if (pPickups == null) pPickups = new HashMap<>();
            pPickups.put(username, pickupPoint);
            trip.setPassengerPickupPoints(pPickups);
        }
        
        // Phân loại vào danh sách online tương ứng
        if ("CASH".equals(paymentType)) {
            pUnpaid.addAll(seatNumbers);
        } else if ("PASS".equals(paymentType)) {
            // Thanh toán bằng thẻ vé (0 VNĐ) - Cho thẳng vào paid
            pPaid.addAll(seatNumbers);
        } else {
            // "TRANSFER" - Mặc định là onlinePaidSeats
            pPaid.addAll(seatNumbers);
        }
        
        trip.setPassengerSeats(pSeats);
        trip.setOnlineUnpaidSeats(pUnpaid);
        trip.setOnlinePaidSeats(pPaid);
        
        // Lưu vé vào Lịch sử Tickets cá nhân
        Ticket newTicket = new Ticket();
        newTicket.setUsername(username);
        newTicket.setRouteName(trip.getRouteName());
        newTicket.setPickupPoint((pickupPoint != null && !pickupPoint.isEmpty()) ? pickupPoint : "Chưa xác định");
        newTicket.setPaymentMethod(paymentType);
        newTicket.setSeats(seatNumbers);
        newTicket.setTotalAmount("PASS".equals(paymentType) ? 0 : (seatNumbers.size() * 10000L)); // vé lẻ 10k
        newTicket.setBookingTime(LocalDateTime.now());
        
        ticketMongoRepository.save(newTicket);
        
        return ResponseEntity.ok(activeTripRepository.save(trip));
    }
}
