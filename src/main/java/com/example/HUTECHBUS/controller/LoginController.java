package com.example.HUTECHBUS.controller;

import com.example.HUTECHBUS.model.User;
<<<<<<< HEAD
import com.example.HUTECHBUS.repository.TicketPassRepository;
import com.example.HUTECHBUS.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
=======
import com.example.HUTECHBUS.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
>>>>>>> bed3500435ebea4b61c6dcf60486cd4f095c2c85
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

/**
 * Xử lý các trang điều hướng chính: đăng nhập, đăng ký, dashboard, và thông báo.
 */
@Controller
public class LoginController {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketPassRepository ticketPassRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    /** Hiển thị trang đăng ký */
    @GetMapping("/register")
    public String register() {
        return "register";
    }

    /** Xử lý form đăng ký */
    @PostMapping("/register")
    public String registerUser(
            @RequestParam String fullName,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String role,
            RedirectAttributes redirectAttributes
    ) {
        // Chặn tạo tài khoản Admin, Staff, Driver từ public form (tránh frontend bị sửa)
        if (role.equals("ADMIN") || role.equals("STAFF") || role.equals("DRIVER")) {
            redirectAttributes.addFlashAttribute("error", "Loại tài khoản này không được phép tạo tự do!");
            return "redirect:/register";
        }

        String parsedUsername = username.trim();
        // Kiểm tra xem User đã tồn tại chưa
        if (userRepository.findByUsername(parsedUsername).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Mã số này đã được đăng ký!");
            return "redirect:/register";
        }

        // Tạo tài khoản mới
        User user = new User();
        user.setFullName(fullName.trim());
        user.setUsername(parsedUsername);
        user.setPassword(passwordEncoder.encode(password));
        
        Set<String> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        userRepository.save(user);

        // Chuyển hướng kèm tin nhắn thành công
        redirectAttributes.addAttribute("logout", "true"); // Trick để hiện message xanh bên form login
        return "redirect:/login";
    }

    /**
     * Hiển thị trang dashboard chính.
<<<<<<< HEAD
     * Chuyển hướng theo Role (Admin/Driver/Student).
=======
>>>>>>> bed3500435ebea4b61c6dcf60486cd4f095c2c85
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

    /** Hiển thị trang thông báo hệ thống */
    @GetMapping("/notifications")
    public String notifications(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        return "notifications";
    }
}
