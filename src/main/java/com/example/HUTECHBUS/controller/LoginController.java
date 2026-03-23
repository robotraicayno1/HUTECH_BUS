package com.example.HUTECHBUS.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

import com.example.HUTECHBUS.repository.UserRepository;
import com.example.HUTECHBUS.model.User;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Xử lý các trang điều hướng chính: đăng nhập, dashboard, và thông báo.
 */
@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepository;

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
    public String dashboard(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
            userRepository.findByUsername(principal.getName()).ifPresent(user -> {
                model.addAttribute("user", user);
            });
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
