package com.example.HUTECHBUS.controller;

import com.example.HUTECHBUS.model.User;
import com.example.HUTECHBUS.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
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
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
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
