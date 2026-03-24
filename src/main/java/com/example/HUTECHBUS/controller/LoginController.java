package com.example.HUTECHBUS.controller;
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
     * Truyền thông tin người dùng vào model để hiển thị.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        if (authentication == null) return "redirect:/login";

        // Kiểm tra role MANAGER để chuyển hướng sang trang tài xế
        boolean isManager = authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_MANAGER"));
        if (isManager) {
            return "redirect:/driver/app";
        }

        // Kiểm tra role ADMIN để chuyển hướng sang trang admin
        boolean isAdmin = authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (isAdmin) {
            return "redirect:/admin";
        }

        model.addAttribute("username", authentication.getName());
        userRepository.findByUsername(authentication.getName()).ifPresent(user -> {
            model.addAttribute("user", user);
            if (user.getActivePassId() != null) {
                ticketPassRepository.findById(user.getActivePassId()).ifPresent(pass -> {
                    model.addAttribute("activePass", pass);
                });
            }
        });
        return "index";
    }

    /** Hiển thị trang thông báo hệ thống */
    @GetMapping("/notifications")
    public String notifications(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        return "notifications";
    }
}
