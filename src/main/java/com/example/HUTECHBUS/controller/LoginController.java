package com.example.HUTECHBUS.controller;

import com.example.HUTECHBUS.model.User;
import com.example.HUTECHBUS.repository.TicketPassRepository;
import com.example.HUTECHBUS.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

/**
 * Xử lý các trang điều hướng chính: đăng nhập, dashboard, và thông báo.
 */
@Controller
public class LoginController {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketPassRepository ticketPassRepository;

    /** Hiển thị trang đăng nhập */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /** Chuyển hướng gốc về trang đăng nhập */
    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    /**
     * Hiển thị trang dashboard chính.
     * Chuyển hướng theo Role (Admin/Driver/Student).
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        if (authentication == null) return "redirect:/login";

        // 1. Kiểm tra role MANAGER (Tài xế/Tiếp viên)
        boolean isManager = authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_MANAGER"));
        if (isManager) {
            return "redirect:/driver/app";
        }

        // 2. Kiểm tra role ADMIN
        boolean isAdmin = authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (isAdmin) {
            return "redirect:/admin";
        }

        // 3. Xử lý cho Sinh viên (STUDENT)
        String username = authentication.getName();
        model.addAttribute("username", username);
        
        userRepository.findByUsername(username).ifPresent(user -> {
            model.addAttribute("user", user);
            // Gửi cả thông tin Thẻ vé nếu có
            if (user.getActivePassId() != null) {
                ticketPassRepository.findById(user.getActivePassId()).ifPresent(pass -> {
                    model.addAttribute("activePass", pass);
                });
            }
        });
        
        return "index";
    }

}

