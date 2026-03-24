package com.example.HUTECHBUS.controller;

import com.example.HUTECHBUS.model.Ticket;
import com.example.HUTECHBUS.model.TicketPass;
import com.example.HUTECHBUS.repository.TicketPassRepository;
import com.example.HUTECHBUS.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/driver")
public class DriverApiController {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketPassRepository ticketPassRepository;

    @GetMapping("/scan-qr")
    public ResponseEntity<?> scanQrCode(@RequestParam("ticketId") String qrData) {
        String message = "Vé hợp lệ";
        boolean isPassUser = false;
        boolean isValid = false;
        Ticket ticket = null;
        TicketPass pass = null;
        
        Date now = new Date();

        // 1. Nếu là QR "Check-in Nhanh" từ Dashboard (Định dạng: HUTECHBUS-username)
        if (qrData.startsWith("HUTECHBUS-") && !qrData.contains("|")) {
            String username = qrData.substring("HUTECHBUS-".length());
            
            List<TicketPass> passes = ticketPassRepository.findByUsernameAndStatus(username, "ACTIVE");
            Optional<TicketPass> activePassOpt = passes.stream()
                .filter(p -> p.getStartDate() != null && p.getExpiryDate() != null 
                            && p.getStartDate().before(now) && p.getExpiryDate().after(now))
                .findFirst();
            
            if (activePassOpt.isPresent()) {
                TicketPass activePass = activePassOpt.get();
                pass = activePass;
                message = "Thẻ Định Kỳ Hợp Lệ - Mời Lên Xe";
                isPassUser = true;
                isValid = true;
                
                // Tự động tìm 1 ghế trống bất kỳ (Simulate)
                ticket = new Ticket();
                ticket.setId("HT" + (int)(Math.random() * 1000000));
                ticket.setUsername(username);
                ticket.setRoute(activePass.getRoute());
                ticket.setDate(new java.text.SimpleDateFormat("dd/MM/yyyy").format(now));
                ticket.setTime(new java.text.SimpleDateFormat("hh:mm a").format(now));
                ticket.setSeats("Ghế Trống (Auto Pass)");
                ticket.setTotal(0.0);
                ticket.setStatus("success");
                ticketRepository.save(ticket);
            } else {
                message = "Thẻ Hết Hạn - Vui Lòng Trả Tiền Mặt";
            }
        } 
        // 2. Nếu là QR từ Vé (Định dạng: TICKET_ID:HTxxxx|... hoặc TICKET_ID:PSxxxx|...)
        else if (qrData.contains("TICKET_ID:")) {
            String idToken = "TICKET_ID:";
            int startIndex = qrData.indexOf(idToken) + idToken.length();
            int endIndex = qrData.indexOf("|", startIndex);
            if(endIndex == -1) endIndex = qrData.length();
            String realTicketId = qrData.substring(startIndex, endIndex);
            
            if (realTicketId.startsWith("PS-")) {
                Optional<TicketPass> optPass = ticketPassRepository.findById(realTicketId);
                if (optPass.isEmpty()) {
                    return ResponseEntity.status(404).body(Map.of("error", "Mã thẻ không tồn tại"));
                }
                pass = optPass.get();
                boolean isActiveAndValid = "ACTIVE".equalsIgnoreCase(pass.getStatus()) 
                                        && pass.getStartDate() != null && pass.getExpiryDate() != null 
                                        && pass.getStartDate().before(now) && pass.getExpiryDate().after(now);
                if (isActiveAndValid) {
                    message = "Thẻ Định Kỳ Hợp Lệ - Mời Lên Xe";
                    isPassUser = true;
                    isValid = true;
                } else {
                    message = "Thẻ Hết Hạn - Vui Lòng Trả Tiền Mặt";
                    isValid = false;
                }
            } else {
                Optional<Ticket> optTicket = ticketRepository.findById(realTicketId);
                if (optTicket.isEmpty()) {
                    return ResponseEntity.status(404).body(Map.of("error", "Vé không tồn tại"));
                }
                ticket = optTicket.get();
                
                String currentRoute = ticket.getRoute();
                List<TicketPass> passes = ticketPassRepository.findByUsernameAndStatus(ticket.getUsername(), "ACTIVE");
                boolean hasActivePass = passes.stream()
                    .anyMatch(p -> p.getStartDate() != null && p.getExpiryDate() != null 
                                && p.getStartDate().before(now) && p.getExpiryDate().after(now)
                                && p.getRoute() != null && p.getRoute().equals(currentRoute));
                
                if (hasActivePass) {
                    message = "Thẻ Định Kỳ Hợp Lệ - Mời Lên Xe";
                    isPassUser = true;
                    isValid = true;
                } else if (!"success".equals(ticket.getStatus()) && !"confirmed".equals(ticket.getStatus())) {
                    message = "Vé chưa thanh toán hoặc bị từ chối";
                    isValid = false;
                } else {
                    isValid = true;
                }
            }
        }
        // 3. Fallback: Nếu gửi ticketId thật (tương thích API cũ)
        else {
            if (qrData.startsWith("PS-")) {
                Optional<TicketPass> optPass = ticketPassRepository.findById(qrData);
                if (optPass.isPresent()) {
                    pass = optPass.get();
                    boolean isActiveAndValid = "ACTIVE".equalsIgnoreCase(pass.getStatus()) 
                                            && pass.getStartDate() != null && pass.getExpiryDate() != null 
                                            && pass.getStartDate().before(now) && pass.getExpiryDate().after(now);
                    if (isActiveAndValid) {
                        message = "Thẻ Định Kỳ Hợp Lệ - Mời Lên Xe";
                        isPassUser = true;
                        isValid = true;
                    } else {
                        message = "Thẻ Hết Hạn - Vui Lòng Trả Tiền Mặt";
                        isValid = false;
                    }
                } else {
                    return ResponseEntity.status(404).body(Map.of("error", "Mã thẻ không tồn tại"));
                }
            } else {
                Optional<Ticket> optTicket = ticketRepository.findById(qrData);
                if (optTicket.isPresent()) {
                    ticket = optTicket.get();
                    String currentRoute = ticket.getRoute();
                    List<TicketPass> passes = ticketPassRepository.findByUsernameAndStatus(ticket.getUsername(), "ACTIVE");
                    boolean hasActivePass = passes.stream()
                        .anyMatch(p -> p.getStartDate() != null && p.getExpiryDate() != null 
                                    && p.getStartDate().before(now) && p.getExpiryDate().after(now)
                                    && p.getRoute() != null && p.getRoute().equals(currentRoute));
                    
                    if (hasActivePass) {
                        message = "Thẻ Định Kỳ Hợp Lệ - Mời Lên Xe";
                        isPassUser = true;
                        isValid = true;
                    } else if (!"success".equals(ticket.getStatus()) && !"confirmed".equals(ticket.getStatus())) {
                        message = "Vé chưa thanh toán hoặc bị từ chối";
                    } else {
                        isValid = true;
                    }
                } else {
                    return ResponseEntity.status(404).body(Map.of("error", "Mã QR không hợp lệ hoặc Không tìm thấy vé"));
                }
            }
        }

        return ResponseEntity.ok(Map.of(
            "ticket", ticket != null ? ticket : "N/A",
            "pass", pass != null ? pass : "N/A",
            "message", message,
            "isPassUser", isPassUser,
            "valid", isValid
        ));
    }
}
