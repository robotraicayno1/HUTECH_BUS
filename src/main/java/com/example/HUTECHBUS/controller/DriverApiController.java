package com.example.HUTECHBUS.controller;

import com.example.HUTECHBUS.model.ActiveTrip;
import com.example.HUTECHBUS.model.Route;
import com.example.HUTECHBUS.model.User;
import com.example.HUTECHBUS.repository.ActiveTripRepository;
import com.example.HUTECHBUS.repository.RouteRepository;
import com.example.HUTECHBUS.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
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
    private ActiveTripRepository activeTripRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private UserRepository userRepository;

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
        return ResponseEntity.ok(Map.of("message", "Da ket thuc chuyen xe"));
    }

    @PostMapping("/{tripId}/toggle-seat")
    public ResponseEntity<?> toggleSeat(@PathVariable String tripId,
                                        @RequestBody Map<String, Integer> payload,
                                        Principal principal) {
        Optional<ActiveTrip> tripOpt = activeTripRepository.findById(tripId);
        if (tripOpt.isEmpty()) return ResponseEntity.notFound().build();

        ActiveTrip trip = tripOpt.get();
        if (principal != null && !trip.getDriverId().equals(principal.getName())) {
            return ResponseEntity.status(403).body("Forbidden");
        }

        Integer seatNumber = payload.get("seatNumber");
        if (seatNumber == null || seatNumber < 1 || seatNumber > trip.getTotalSeats()) {
            return ResponseEntity.badRequest().body("So ghe khong hop le.");
        }

        List<Integer> locked = trip.getLockedSeats();
        if (locked.contains(seatNumber)) {
            locked.remove(seatNumber);
        } else {
            locked.add(seatNumber);
        }
        trip.setLockedSeats(locked);
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

        Map<String, Integer> pSeats = trip.getPassengerSeats();
        List<Integer> locked = trip.getLockedSeats();

        if (pSeats.containsKey(username)) {
            return ResponseEntity.ok(Map.of(
                    "message", "Sinh vien da co ghe.",
                    "studentName", student.getFullName(),
                    "seatNumber", pSeats.get(username),
                    "trip", trip));
        }

        int assignedSeat = -1;
        for (int i = 1; i <= trip.getTotalSeats(); i++) {
            if (!locked.contains(i)) {
                assignedSeat = i;
                break;
            }
        }

        if (assignedSeat == -1) {
            return ResponseEntity.badRequest().body("Xe het ghe trong.");
        }

        pSeats.put(username, assignedSeat);
        locked.add(assignedSeat);
        trip.setPassengerSeats(pSeats);
        trip.setLockedSeats(locked);
        ActiveTrip saved = activeTripRepository.save(trip);

        return ResponseEntity.ok(Map.of(
                "message", "Da xep ghe thanh cong.",
                "studentName", student.getFullName(),
                "seatNumber", assignedSeat,
                "trip", saved));
    }

    @GetMapping("/routes")
    public ResponseEntity<?> getRoutes() {
        return ResponseEntity.ok(routeRepository.findAll());
    }
}
