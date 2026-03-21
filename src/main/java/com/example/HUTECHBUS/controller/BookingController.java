package com.example.HUTECHBUS.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BookingController {
    
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
}
