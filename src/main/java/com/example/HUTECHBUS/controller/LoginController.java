package com.example.HUTECHBUS.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

/**
 * Xử lý các trang điều hướng chính: đăng nhập, dashboard, và thông báo.
 */
@Controller
public class LoginController {

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
        if (principal == null) return "redirect:/login";

        // Kiểm tra xem người dùng có phải là tài xế không (tạm thời kiểm tra qua username/database nếu cần)
        // Ở đây đơn giản là nếu username bắt đầu bằng 'driver' thì chuyển sang trang tài xế
        // Hoặc tốt nhất là để người dùng tự chọn, nhưng theo yêu cầu "về dashboard chính tài khoản đó"
        // Ta sẽ kiểm tra ROLE nếu có tích hợp đầy đủ, ở đây ta giả định driver01...
        if (principal.getName().startsWith("driver")) {
            return "redirect:/driver/app";
        }

        model.addAttribute("username", principal.getName());
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
