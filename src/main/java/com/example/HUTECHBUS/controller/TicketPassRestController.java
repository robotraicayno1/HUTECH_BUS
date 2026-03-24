package com.example.HUTECHBUS.controller;

import com.example.HUTECHBUS.model.TicketPass;
import com.example.HUTECHBUS.repository.TicketPassRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/passes")
public class TicketPassRestController {

    @Autowired
    private TicketPassRepository ticketPassRepository;

    @GetMapping("/my")
    public ResponseEntity<?> getMyActivePass(Principal principal, @RequestParam(required = false) String route) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        String username = principal.getName();
        List<TicketPass> passes;
        if (route != null && !route.isEmpty()) {
            passes = ticketPassRepository.findByUsernameAndRouteAndStatus(username, route, "ACTIVE");
        } else {
            passes = ticketPassRepository.findByUsernameAndStatus(username, "ACTIVE");
        }
        
        Date now = new Date();
        List<TicketPass> activePasses = passes.stream()
            .filter(p -> p.getStartDate().before(now) && p.getExpiryDate().after(now))
            .collect(Collectors.toList());

        if (route != null && !route.isEmpty()) {
            if (!activePasses.isEmpty()) {
                return ResponseEntity.ok(activePasses.get(0));
            } else {
                return ResponseEntity.ok(Map.of("hasPass", false));
            }
        } else {
            return ResponseEntity.ok(activePasses);
        }
    }

    @PostMapping("/buy")
    public ResponseEntity<?> buyPass(@RequestBody Map<String, String> body, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        String username = principal.getName();
        String type = body.get("type"); // DAY, MONTH, YEAR
        String route = body.get("route");

        if (type == null || (!type.equals("DAY") && !type.equals("MONTH") && !type.equals("YEAR"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid pass type"));
        }
        if (route == null || route.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Route is required"));
        }

        TicketPass pass = new TicketPass();
        pass.setId("PS-" + (int)(Math.random() * 1000000));
        pass.setUsername(username);
        pass.setType(type);
        pass.setRoute(route);
        
        Date now = new Date();
        pass.setPurchaseDate(now);
        pass.setStartDate(now);
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        if ("DAY".equals(type)) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        } else if ("MONTH".equals(type)) {
            cal.add(Calendar.MONTH, 1);
        } else if ("YEAR".equals(type)) {
            cal.add(Calendar.YEAR, 1);
        }
        pass.setExpiryDate(cal.getTime());
        pass.setStatus("PENDING"); // Pending payment
        
        TicketPass savedPass = ticketPassRepository.save(pass);
        
        // GIẢ LẬP WEBHOOK NGÂN HÀNG: Tự động đổi trạng thái thành 'ACTIVE' sau 5 giây (Auto Duyệt)
        new Thread(() -> {
            try {
                Thread.sleep(5000); // Đợi 5 giây
                Optional<TicketPass> opt = ticketPassRepository.findById(savedPass.getId());
                if (opt.isPresent() && "PENDING".equals(opt.get().getStatus())) {
                    TicketPass t = opt.get();
                    t.setStatus("ACTIVE");
                    ticketPassRepository.save(t);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        return ResponseEntity.ok(savedPass);
    }
}
