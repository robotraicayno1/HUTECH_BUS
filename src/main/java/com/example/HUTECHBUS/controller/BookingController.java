package com.example.HUTECHBUS.controller;

import com.example.HUTECHBUS.model.ActiveTrip;
import com.example.HUTECHBUS.model.TicketPass;
import com.example.HUTECHBUS.model.Ticket;
import com.example.HUTECHBUS.model.User;
import com.example.HUTECHBUS.repository.ActiveTripMongoRepository;
import com.example.HUTECHBUS.repository.TicketPassRepository;
import com.example.HUTECHBUS.repository.PassPackageRepository;
import com.example.HUTECHBUS.repository.TicketMongoRepository;
import com.example.HUTECHBUS.repository.UserRepository;
import com.example.HUTECHBUS.repository.UserVoucherRepository;
import com.example.HUTECHBUS.model.UserVoucher;
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

    @Autowired
    private PassPackageRepository passPackageRepository;

    @Autowired
    private UserVoucherRepository userVoucherRepository;

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
            String username = principal.getName();
            userRepository.findByUsername(username).ifPresent(user -> {
                model.addAttribute("hPoints", user.getHPoints());
            });
            // Load all available Pass Packages for purchase
            java.util.List<com.example.HUTECHBUS.model.PassPackage> packages = passPackageRepository.findAll();
            model.addAttribute("packages", packages);

            // Load user's passes assigned/created by admin
            List<TicketPass> myPasses = ticketPassRepository.findByUserId(username);
            model.addAttribute("myPasses", myPasses);
            model.addAttribute("hasActive", myPasses.stream().anyMatch(p -> "ACTIVE".equals(p.getStatus())));
            
            // Tải danh sách Voucher có thể dùng để mua thẻ (MỚI)
            userRepository.findByUsername(username).ifPresent(user -> {
                model.addAttribute("myVouchers", userVoucherRepository.findByUserIdAndStatus(user.getId(), "ACTIVE"));
            });
        }
        return "buy-pass";
    }

    // --- CÁC API ĐỒNG BỘ VỚI DRIVER ---

    @GetMapping("/api/users/me/active-pass")
    @ResponseBody
    public ResponseEntity<?> getActivePass(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body("Yêu cầu đăng nhập.");
        String username = principal.getName();
        
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // 1. Kiểm tra field activePassId trên User
            if (user.getActivePassId() != null) {
                Optional<TicketPass> passOpt = ticketPassRepository.findById(user.getActivePassId());
                if (passOpt.isPresent() && "ACTIVE".equals(passOpt.get().getStatus())) {
                    return ResponseEntity.ok(Map.of("hasActivePass", true, "pass", passOpt.get()));
                }
            }
        }
        
        // 2. Dự phòng: Tìm theo username và status ACTIVE (Dành cho re-seed hoặc lỗi link)
        // Ưu tiên thẻ có ngày hết hạn xa nhất
        Optional<TicketPass> fallbackPass = ticketPassRepository.findByUserId(username).stream()
            .filter(p -> "ACTIVE".equals(p.getStatus()))
            .filter(p -> p.getExpiryDate().isAfter(LocalDateTime.now()))
            .sorted((p1, p2) -> p2.getExpiryDate().compareTo(p1.getExpiryDate()))
            .findFirst();

        if (fallbackPass.isPresent()) {
            return ResponseEntity.ok(Map.of("hasActivePass", true, "pass", fallbackPass.get()));
        }
        
        return ResponseEntity.ok(Map.of("hasActivePass", false));
    }

    /**
     * Lấy danh sách voucher chưa sử dụng của tôi.
     */
    @GetMapping("/api/bookings/vouchers")
    @ResponseBody
    public ResponseEntity<?> getMyVouchers(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body("Yêu cầu đăng nhập.");
        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        
        return ResponseEntity.ok(userVoucherRepository.findByUserIdAndStatus(user.getId(), "ACTIVE"));
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
     * Lấy thông tin chi tiết của một chuyến xe cụ thể bằng ID.
     */
    @GetMapping("/api/bookings/active-trip/{tripId}")
    @ResponseBody
    public ResponseEntity<?> getActiveTripById(@PathVariable String tripId) {
        return activeTripRepository.findById(tripId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Đặt chỗ và khóa ghế trên chuyến xe đang chạy.
     */
    @PostMapping("/api/bookings/reserve")
    @ResponseBody
    public ResponseEntity<?> reserveSeats(@RequestBody Map<String, Object> payload, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body("Yêu cầu đăng nhập.");
        
        String routeId = (String) payload.get("routeId");
        String tripId = (String) payload.get("tripId");
        String paymentType = (String) payload.getOrDefault("paymentType", "TRANSFER");
        String voucherId = (String) payload.get("voucherId");
        @SuppressWarnings("unchecked")
        List<Integer> seatNumbers = (List<Integer>) payload.get("seatNumbers");
        String username = principal.getName();

        Optional<ActiveTrip> tripOpt = Optional.empty();
        if (tripId != null && !tripId.isEmpty()) {
            tripOpt = activeTripRepository.findById(tripId);
        } else {
            tripOpt = activeTripRepository.findByRouteIdAndStatus(routeId, "RUNNING").stream().findFirst();
        }

        if (tripOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Hiện không có chuyến xe nào đang chạy.");
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
        
        // Cập nhật điểm đón/đến nếu có
        String pickupPoint = (String) payload.get("pickupPoint");
        String dropoffPoint = (String) payload.get("dropoffPoint");
        
        if (pickupPoint != null && !pickupPoint.isEmpty()) {
            Map<String, String> pPickups = trip.getPassengerPickupPoints();
            if (pPickups == null) pPickups = new HashMap<>();
            pPickups.put(username, pickupPoint);
            trip.setPassengerPickupPoints(pPickups);
        }
        
        if (dropoffPoint != null && !dropoffPoint.isEmpty()) {
            Map<String, String> pDropoffs = trip.getPassengerDropoffPoints();
            if (pDropoffs == null) pDropoffs = new HashMap<>();
            pDropoffs.put(username, dropoffPoint);
            trip.setPassengerDropoffPoints(pDropoffs);
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
        
        long totalAmount = (seatNumbers.size() * 10000L);
        if ("PASS".equals(paymentType)) {
            totalAmount = 0;
        } else if (voucherId != null && !voucherId.isEmpty()) {
            Optional<UserVoucher> uvOpt = userVoucherRepository.findById(voucherId);
            if (uvOpt.isPresent()) {
                UserVoucher uv = uvOpt.get();
                if ("ACTIVE".equals(uv.getStatus())) {
                    totalAmount = Math.max(0, totalAmount - uv.getDiscountAmount());
                    uv.setStatus("USED");
                    userVoucherRepository.save(uv);
                }
            }
        }
        
        newTicket.setTotalAmount(totalAmount); 
        newTicket.setBookingTime(LocalDateTime.now());
        
        ticketMongoRepository.save(newTicket);
        
        return ResponseEntity.ok(activeTripRepository.save(trip));
    }
}
