package com.example.HUTECHBUS.controller;

import com.example.HUTECHBUS.model.ActiveTrip;
import com.example.HUTECHBUS.model.Notification;
import com.example.HUTECHBUS.model.Route;
import com.example.HUTECHBUS.model.Ticket;
import com.example.HUTECHBUS.model.TripHistory;
import com.example.HUTECHBUS.model.User;
import com.example.HUTECHBUS.repository.ActiveTripMongoRepository;
import com.example.HUTECHBUS.repository.NotificationRepository;
import com.example.HUTECHBUS.repository.RouteRepository;
import com.example.HUTECHBUS.repository.TicketMongoRepository;
import com.example.HUTECHBUS.repository.TripHistoryRepository;
import com.example.HUTECHBUS.repository.TicketPassRepository;
import com.example.HUTECHBUS.repository.UserRepository;
import com.example.HUTECHBUS.model.TicketPass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST API Controller - xu ly tat ca cac endpoint /api/driver/trips/*.
 */
@RestController
@RequestMapping("/api/driver/trips")
public class DriverApiController {

    @Autowired
    private ActiveTripMongoRepository activeTripRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TripHistoryRepository tripHistoryRepository;

    @Autowired
    private TicketPassRepository ticketPassRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TicketMongoRepository ticketMongoRepository;

    @GetMapping("/active")
    public ResponseEntity<?> getActiveTrip(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body("Unauthorized");
        Optional<ActiveTrip> trip = activeTripRepository.findByDriverIdAndStatus(principal.getName(), "RUNNING");
        return trip.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/start")
    public ResponseEntity<?> startTrip(@RequestBody Map<String, String> payload, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body("Unauthorized");
        String driverId = principal.getName();
        String routeId = payload.get("routeId");

        if (activeTripRepository.findByDriverIdAndStatus(driverId, "RUNNING").isPresent()) {
            return ResponseEntity.badRequest().body("Ban da co chuyen dang chay.");
        }

        Optional<Route> routeOpt = routeRepository.findById(routeId);
        if (routeOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Khong tim thay route.");
        }

        ActiveTrip newTrip = new ActiveTrip();
        newTrip.setDriverId(driverId);
        newTrip.setRouteId(routeId);
        newTrip.setRouteName(routeOpt.get().getName());
        newTrip.setStartTime(LocalDateTime.now());
        newTrip.getLockedSeats().clear();
        newTrip.getPassengerSeats().clear();

        return ResponseEntity.ok(activeTripRepository.save(newTrip));
    }

    @PostMapping("/{tripId}/end")
    public ResponseEntity<?> endTrip(@PathVariable String tripId, Principal principal) {
        Optional<ActiveTrip> tripOpt = activeTripRepository.findById(tripId);
        if (tripOpt.isEmpty()) return ResponseEntity.notFound().build();

        ActiveTrip trip = tripOpt.get();
        if (principal != null && !trip.getDriverId().equals(principal.getName())) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        trip.setStatus("COMPLETED");
        activeTripRepository.save(trip);

        // --- GHI LẠI LỊCH SỬ VÀ GỬI THÔNG BÁO CHO TẤT CẢ SINH VIÊN TRÊN XE ---
        Map<String, List<Integer>> passengers = trip.getPassengerSeats();
        if (passengers != null) {
            passengers.forEach((username, seats) -> {
                Optional<User> userOpt = userRepository.findByUsername(username);
                if (userOpt.isPresent()) {
                    User student = userOpt.get();
                    // Ghi lịch sử
                    TripHistory history = new TripHistory();
                    history.setUserId(student.getId());
                    history.setRouteId(trip.getRouteId());
                    history.setRouteName(trip.getRouteName());
                    history.setTripDate(LocalDateTime.now());
                    history.setStatus("COMPLETED");
                    tripHistoryRepository.save(history);

                    // Đánh dấu Ticket của sinh viên là COMPLETED
                    // Dùng findAll theo username để bắt cả vé cũ có status = null
                    List<Ticket> userTickets = ticketMongoRepository.findByUsernameOrderByBookingTimeDesc(username);
                    for (Ticket t : userTickets) {
                        // Bỏ qua vé đã hoàn thành rồi
                        if ("COMPLETED".equals(t.getStatus())) continue;
                        // Chỉ đánh dấu vé cùng tuyến xe + ghế trùng khớp
                        boolean sameRoute = trip.getRouteName() != null && trip.getRouteName().equals(t.getRouteName());
                        boolean seatMatch = t.getSeats() != null && t.getSeats().stream().anyMatch(seats::contains);
                        if (sameRoute && seatMatch) {
                            t.setStatus("COMPLETED");
                            t.setTripId(tripId);
                            t.setCompletedAt(LocalDateTime.now());
                            ticketMongoRepository.save(t);
                        }
                    }

                    // Gửi thông báo
                    Notification noti = new Notification();
                    noti.setUserId(username);
                    noti.setTitle("✅ Chuyến xe đã hoàn thành!");
                    noti.setMessage("Chuyến xe " + trip.getRouteName() + " của bạn đã kết thúc lúc "
                            + LocalDateTime.now().getHour() + "h"
                            + String.format("%02d", LocalDateTime.now().getMinute())
                            + ". Ghế: " + seats + ". Cảm ơn bạn đã sử dụng HUTECHBUS! (+10 H-Point)");
                    noti.setType("TRIP_COMPLETE");
                    notificationRepository.save(noti);
                }
            });
        }

        return ResponseEntity.ok(Map.of("message", "Da ket thuc chuyen xe va ghi nhan lich su"));
    }

    @PostMapping("/{tripId}/toggle-seat")
    public ResponseEntity<?> toggleSeat(@PathVariable String tripId,
                                        @RequestBody Map<String, Object> payload,
                                        Principal principal) {
        Optional<ActiveTrip> tripOpt = activeTripRepository.findById(tripId);
        if (tripOpt.isEmpty()) return ResponseEntity.notFound().build();

        ActiveTrip trip = tripOpt.get();
        if (principal != null && !trip.getDriverId().equals(principal.getName())) {
            return ResponseEntity.status(403).body("Forbidden");
        }

        Integer seatNumber = (Integer) payload.get("seatNumber");
        String paymentType = (String) payload.getOrDefault("paymentType", "CASH");
        
        if (seatNumber == null || seatNumber < 1 || seatNumber > trip.getTotalSeats()) {
            return ResponseEntity.badRequest().body("So ghe khong hop le.");
        }

        List<Integer> locked = trip.getLockedSeats();
        List<Integer> tPaid = trip.getTransferPaidSeats();
        List<Integer> pPaid = trip.getOnlinePaidSeats();
        List<Integer> pUnpaid = trip.getOnlineUnpaidSeats();

        // Nếu đã ở trong bất kỳ danh sách nào -> Mở khóa (Xóa hết)
        if (locked.contains(seatNumber)) {
            locked.remove(seatNumber);
        } else if (tPaid.contains(seatNumber)) {
            tPaid.remove(seatNumber);
        } else if (pPaid.contains(seatNumber)) {
            pPaid.remove(seatNumber);
        } else if (pUnpaid.contains(seatNumber)) {
            pUnpaid.remove(seatNumber);
        } else {
            // Khóa mới
            if ("TRANSFER".equals(paymentType)) {
                tPaid.add(seatNumber);
            } else {
                locked.add(seatNumber);
            }
        }

        trip.setLockedSeats(locked);
        trip.setTransferPaidSeats(tPaid);
        trip.setOnlinePaidSeats(pPaid);
        trip.setOnlineUnpaidSeats(pUnpaid);
        return ResponseEntity.ok(activeTripRepository.save(trip));
    }

    @PostMapping("/{tripId}/scan-qr")
    public ResponseEntity<?> scanQr(@PathVariable String tripId,
                                    @RequestBody Map<String, String> payload,
                                    Principal principal) {
        Optional<ActiveTrip> tripOpt = activeTripRepository.findById(tripId);
        if (tripOpt.isEmpty()) return ResponseEntity.notFound().build();

        ActiveTrip trip = tripOpt.get();
        if (principal != null && !trip.getDriverId().equals(principal.getName())) {
            return ResponseEntity.status(403).body("Forbidden");
        }

        String qrText = payload.get("qrText");
        if (qrText == null || !qrText.startsWith("HUTECHBUS-")) {
            return ResponseEntity.badRequest().body("Ma QR khong hop le.");
        }

        String username = qrText.replace("HUTECHBUS-", "");
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Khong tim thay sinh vien.");
        }
        User student = userOpt.get();

        Map<String, List<Integer>> pSeats = trip.getPassengerSeats();
        List<Integer> checkedIn = trip.getCheckedInSeats();

        if (!pSeats.containsKey(username)) {
            return ResponseEntity.badRequest().body("Sinh vien nay chua dat ve cho chuyen xe nay!");
        }

        List<Integer> userSeats = pSeats.get(username);
        boolean newCheckIn = false;
        for (Integer s : userSeats) {
            if (!checkedIn.contains(s)) {
                checkedIn.add(s);
                newCheckIn = true;
            }
        }
        
        if (!newCheckIn) {
            return ResponseEntity.badRequest().body("Sinh vien nay da check-in tat ca cac ghe.");
        }
        
        trip.setCheckedInSeats(checkedIn);
        ActiveTrip saved = activeTripRepository.save(trip);

        // Cộng 10 điểm thưởng cho mỗi lần đi xe (Check-in thành công)
        student.setHPoints(student.getHPoints() + 10);
        userRepository.save(student);

        // Kiểm tra thẻ vé
        String passStatus = "NONE";
        String passType = "NONE";
        if (student.getActivePassId() != null) {
            Optional<TicketPass> passOpt = ticketPassRepository.findById(student.getActivePassId());
            if (passOpt.isPresent()) {
                TicketPass pass = passOpt.get();
                passStatus = "ACTIVE".equals(pass.getStatus()) ? "VALID" : "EXPIRED";
                passType = pass.getType();
            }
        }

        return ResponseEntity.ok(Map.of(
                "message", "Xac nhan len xe thanh cong (+10 H-Point).",
                "studentName", student.getFullName(),
                "seatNumber", userSeats.get(0),
                "passStatus", passStatus,
                "passType", passType,
                "trip", saved));
    }

    @GetMapping("/{tripId}/passengers")
    public ResponseEntity<?> getPassengers(@PathVariable String tripId) {
        Optional<ActiveTrip> tripOpt = activeTripRepository.findById(tripId);
        if (tripOpt.isEmpty()) return ResponseEntity.notFound().build();

        ActiveTrip trip = tripOpt.get();
        Map<String, List<Integer>> passengersMap = trip.getPassengerSeats();
        List<Map<String, Object>> passengerList = new ArrayList<>();

        if (passengersMap != null) {
            passengersMap.forEach((username, seats) -> {
                Optional<User> userOpt = userRepository.findByUsername(username);
                Map<String, Object> pInfo = new HashMap<>();
                pInfo.put("username", username);
                pInfo.put("fullName", userOpt.map(User::getFullName).orElse("Unknown"));
                pInfo.put("seats", seats);
                passengerList.add(pInfo);
            });
        }

        return ResponseEntity.ok(passengerList);
    }

    @GetMapping("/routes")
    public ResponseEntity<?> getRoutes() {
        return ResponseEntity.ok(routeRepository.findAll());
    }
}
