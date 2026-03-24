package com.example.HUTECHBUS.controller;

import com.example.HUTECHBUS.model.Ticket;
import com.example.HUTECHBUS.repository.TicketRepository;
import com.example.HUTECHBUS.repository.TicketPassRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Date;

@RestController
@RequestMapping("/api/tickets")
public class TicketRestController {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketPassRepository ticketPassRepository;

    @PostMapping
    public ResponseEntity<?> createTicket(@RequestBody Ticket ticket, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        String username = principal.getName();
        
        ticket.setUsername(username);
        if(ticket.getId() == null) {
            ticket.setId("HT" + (int)(Math.random() * 1000000));
        }
        if(ticket.getStatus() == null) {
            ticket.setStatus("pending");
        }
        
        Date now = new Date();
        List<com.example.HUTECHBUS.model.TicketPass> passes = ticketPassRepository.findByUsernameAndStatus(username, "ACTIVE");
        boolean hasActivePass = passes.stream()
            .anyMatch(p -> p.getStartDate() != null && p.getExpiryDate() != null 
                        && p.getStartDate().before(now) && p.getExpiryDate().after(now)
                        && p.getRoute() != null && p.getRoute().equals(ticket.getRoute()));
        
        if (hasActivePass) {
            ticket.setTotal(0.0);
            // Optional: Auto-approve pass tickets immediately instead of pending payment 
            // since they don't need to pay.
            ticket.setStatus("success");
        }
        
        Ticket savedTicket = ticketRepository.save(ticket);
        
        // GIẢ LẬP WEBHOOK NGÂN HÀNG: Tự động đổi trạng thái thành 'success' sau 5 giây (Auto Duyệt)
        if ("pending".equals(savedTicket.getStatus())) {
            new Thread(() -> {
                try {
                    Thread.sleep(5000); // Đợi 5 giây
                    Optional<Ticket> opt = ticketRepository.findById(savedTicket.getId());
                    if (opt.isPresent() && "pending".equals(opt.get().getStatus())) {
                        Ticket t = opt.get();
                        
                        // Check conflict ghế
                        List<Ticket> existing = ticketRepository.findByRouteAndDateAndTime(t.getRoute(), t.getDate(), t.getTime());
                        java.util.Set<String> bookedSeats = new java.util.HashSet<>();
                        for(Ticket et : existing) {
                            if(!et.getId().equals(t.getId()) && ("success".equals(et.getStatus()) || "confirmed".equals(et.getStatus()))) {
                                if(et.getSeats() != null) {
                                    for(String s : et.getSeats().split(",")) {
                                        bookedSeats.add(s.trim());
                                    }
                                }
                            }
                        }
                        
                        boolean isConflict = false;
                        if (t.getSeats() != null) {
                            for(String s : t.getSeats().split(",")) {
                                if(bookedSeats.contains(s.trim())) {
                                    isConflict = true;
                                    break;
                                }
                            }
                        }
                        
                        if (isConflict) {
                            t.setStatus("failed");
                        } else {
                            t.setStatus("success");
                        }
                        ticketRepository.save(t);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        return ResponseEntity.ok(savedTicket);
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyTickets(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        String username = principal.getName();
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        
        List<Ticket> tickets = ticketRepository.findByUsername(username);
        for (Ticket t : tickets) {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", t.getId());
            map.put("route", t.getRoute());
            map.put("date", t.getDate());
            map.put("time", t.getTime());
            map.put("seats", t.getSeats());
            map.put("total", t.getTotal());
            map.put("status", t.getStatus());
            map.put("type", "TICKET");
            result.add(map);
        }
        
        List<com.example.HUTECHBUS.model.TicketPass> passes = ticketPassRepository.findByUsername(username);
        for (com.example.HUTECHBUS.model.TicketPass p : passes) {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", p.getId());
            map.put("route", p.getRoute());
            map.put("date", new java.text.SimpleDateFormat("dd/MM/yyyy").format(p.getPurchaseDate()));
            map.put("time", "HSD: " + new java.text.SimpleDateFormat("dd/MM/yyyy").format(p.getExpiryDate()));
            map.put("seats", "Thẻ " + p.getType());
            map.put("total", "DAY".equals(p.getType()) ? 10000 : "MONTH".equals(p.getType()) ? 120000 : 950000);
            map.put("status", "PENDING".equals(p.getStatus()) ? "pending" : "EXPIRED".equals(p.getStatus()) || "FAILED".equals(p.getStatus()) ? "failed" : "success");
            map.put("type", "PASS");
            map.put("expiryDate", new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(p.getExpiryDate()));
            result.add(map);
        }
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingTickets(Principal principal) {
        if (principal == null || !principal.getName().toLowerCase().contains("admin")) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }
        
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        
        List<Ticket> tickets = ticketRepository.findByStatus("pending");
        for (Ticket t : tickets) {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", t.getId());
            map.put("username", t.getUsername());
            map.put("route", t.getRoute());
            map.put("date", t.getDate());
            map.put("time", t.getTime());
            map.put("seats", t.getSeats());
            map.put("total", t.getTotal());
            map.put("status", t.getStatus());
            map.put("type", "TICKET");
            result.add(map);
        }
        
        List<com.example.HUTECHBUS.model.TicketPass> passes = ticketPassRepository.findByStatus("PENDING");
        for (com.example.HUTECHBUS.model.TicketPass p : passes) {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", p.getId());
            map.put("username", p.getUsername());
            map.put("route", p.getRoute());
            map.put("date", new java.text.SimpleDateFormat("dd/MM/yyyy").format(p.getPurchaseDate()));
            map.put("time", "");
            map.put("seats", "Thẻ " + p.getType());
            map.put("total", "DAY".equals(p.getType()) ? 10000 : "MONTH".equals(p.getType()) ? 120000 : 950000);
            map.put("status", p.getStatus().toLowerCase()); // pending
            map.put("type", "PASS");
            result.add(map);
        }
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistoryTickets(Principal principal) {
        if (principal == null || !principal.getName().toLowerCase().contains("admin")) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }
        
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        
        List<Ticket> allTickets = ticketRepository.findAll();
        for (Ticket t : allTickets) {
            if (!"pending".equals(t.getStatus())) {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", t.getId());
                map.put("username", t.getUsername());
                map.put("route", t.getRoute());
                map.put("date", t.getDate());
                map.put("time", t.getTime());
                map.put("seats", t.getSeats());
                map.put("total", t.getTotal());
                map.put("status", t.getStatus());
                map.put("type", "TICKET");
                result.add(map);
            }
        }
        
        List<com.example.HUTECHBUS.model.TicketPass> allPasses = ticketPassRepository.findAll();
        for (com.example.HUTECHBUS.model.TicketPass p : allPasses) {
            if (!"PENDING".equalsIgnoreCase(p.getStatus())) {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", p.getId());
                map.put("username", p.getUsername());
                map.put("route", p.getRoute());
                map.put("date", new java.text.SimpleDateFormat("dd/MM/yyyy").format(p.getPurchaseDate()));
                map.put("time", "");
                map.put("seats", "Thẻ " + p.getType());
                map.put("total", "DAY".equals(p.getType()) ? 10000 : "MONTH".equals(p.getType()) ? 120000 : 950000);
                map.put("status", "ACTIVE".equalsIgnoreCase(p.getStatus()) ? "success" : "failed");
                map.put("type", "PASS");
                result.add(map);
            }
        }
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/booked-seats")
    public ResponseEntity<?> getBookedSeats(@RequestParam String route, @RequestParam String date, @RequestParam String time) {
        List<Ticket> tickets = ticketRepository.findByRouteAndDateAndTime(route, date, time);
        java.util.Set<String> bookedSeats = new java.util.HashSet<>();
        for(Ticket t : tickets) {
            if("success".equals(t.getStatus()) || "confirmed".equals(t.getStatus())) {
                if(t.getSeats() != null) {
                    for(String s : t.getSeats().split(",")) {
                        bookedSeats.add(s.trim());
                    }
                }
            }
        }
        return ResponseEntity.ok(bookedSeats);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateTicketStatus(@PathVariable String id, @RequestBody Map<String, String> body, Principal principal) {
        if (principal == null || !principal.getName().toLowerCase().contains("admin")) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }
        
        String newStatus = body.get("status");
        
        if (id.startsWith("PS-")) {
            Optional<com.example.HUTECHBUS.model.TicketPass> optPass = ticketPassRepository.findById(id);
            if (optPass.isPresent()) {
                com.example.HUTECHBUS.model.TicketPass pass = optPass.get();
                if ("success".equals(newStatus)) {
                    pass.setStatus("ACTIVE");
                } else {
                    pass.setStatus("FAILED");
                }
                ticketPassRepository.save(pass);
                return ResponseEntity.ok(Map.of("success", true));
            } else {
                return ResponseEntity.status(404).body(Map.of("error", "Not found"));
            }
        }

        Optional<Ticket> optTicket = ticketRepository.findById(id);
        if (optTicket.isPresent()) {
            Ticket ticket = optTicket.get();
            
            // Logic Check ghế tự động
            if ("success".equals(newStatus) || "confirmed".equals(newStatus)) {
                List<Ticket> existing = ticketRepository.findByRouteAndDateAndTime(ticket.getRoute(), ticket.getDate(), ticket.getTime());
                java.util.Set<String> bookedSeats = new java.util.HashSet<>();
                for(Ticket t : existing) {
                    if(!t.getId().equals(ticket.getId()) && ("success".equals(t.getStatus()) || "confirmed".equals(t.getStatus()))) {
                        if(t.getSeats() != null) {
                            for(String s : t.getSeats().split(",")) {
                                bookedSeats.add(s.trim());
                            }
                        }
                    }
                }
                
                boolean isConflict = false;
                if (ticket.getSeats() != null) {
                    for(String s : ticket.getSeats().split(",")) {
                        if(bookedSeats.contains(s.trim())) {
                            isConflict = true;
                            break;
                        }
                    }
                }
                
                if (isConflict) {
                    ticket.setStatus("failed");
                } else {
                    ticket.setStatus("success");
                }
            } else {
                ticket.setStatus(newStatus);
            }
            
            ticketRepository.save(ticket);
            return ResponseEntity.ok(Map.of("success", true, "ticket", ticket));
        } else {
            return ResponseEntity.status(404).body(Map.of("error", "Not found"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTicket(@PathVariable String id, Principal principal) {
        if (principal == null || !principal.getName().toLowerCase().contains("admin")) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }
        
        if (id.startsWith("PS-")) {
            Optional<com.example.HUTECHBUS.model.TicketPass> optPass = ticketPassRepository.findById(id);
            if (optPass.isPresent()) {
                ticketPassRepository.deleteById(id);
                return ResponseEntity.ok(Map.of("success", true));
            } else {
                return ResponseEntity.status(404).body(Map.of("error", "Not found"));
            }
        }

        Optional<Ticket> optTicket = ticketRepository.findById(id);
        if (optTicket.isPresent()) {
            ticketRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("success", true));
        } else {
            return ResponseEntity.status(404).body(Map.of("error", "Not found"));
        }
    }
}
