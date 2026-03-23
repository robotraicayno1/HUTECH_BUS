package com.example.HUTECHBUS.controller;

import com.example.HUTECHBUS.model.ActiveTrip;
import com.example.HUTECHBUS.repository.ActiveTripMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class BookingController {
    
    @Autowired
    private ActiveTripMongoRepository activeTripRepository;

    
    @GetMapping("/booking")
    public String showBookingPage() {
        return "booking";
    }

    @GetMapping("/ticket")
    public String showTicketPage() {
        return "ticket";
    }

    // Mapping đường dẫn cho trang Xem danh sách vé đã đặt
    @GetMapping("/my-tickets")
    public String showMyTicketsPage() {
        return "my-tickets";
    }

    // --- CÁC API ĐỒNG BỘ VỚI DRIVER ---

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
        
        // Phân loại vào danh sách online tương ứng
        if ("CASH".equals(paymentType)) {
            pUnpaid.addAll(seatNumbers);
        } else {
            // "TRANSFER" - Mặc định là onlinePaidSeats (hoặc đợi VNPAY callback)
            // Lưu ý: Ở bản cũ, VNPAY callback sẽ gọi sau, nên tạm thời cho vào pPaid hoặc danh sách chờ.
            // Để đơn giản theo ý user (đặt là có ghế), ta cho vào pPaid nếu chọn CK, 
            // VNPAY sẽ thực sự chốt sau. 
            pPaid.addAll(seatNumbers);
        }
        
        trip.setPassengerSeats(pSeats);
        trip.setOnlineUnpaidSeats(pUnpaid);
        trip.setOnlinePaidSeats(pPaid);
        
        return ResponseEntity.ok(activeTripRepository.save(trip));
    }
}
