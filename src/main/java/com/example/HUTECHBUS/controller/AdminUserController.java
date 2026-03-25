package com.example.HUTECHBUS.controller;

import com.example.HUTECHBUS.model.User;
import com.example.HUTECHBUS.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminUserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- Students Management ---
    @GetMapping("/students")
    public String manageStudents(Model model, Principal principal) {
        if (principal != null) model.addAttribute("username", principal.getName());
        List<User> students = userRepository.findAll().stream()
                .filter(u -> u.getRoles() != null && (u.getRoles().contains("STUDENT") || u.getRoles().contains("CYU")))
                .collect(Collectors.toList());
        model.addAttribute("users", students);
        model.addAttribute("newUser", new User());
        model.addAttribute("pageType", "student"); // Phân biệt màn hình để quay về đúng route
        return "admin/students";
    }

    // --- Staffs & Drivers Management ---
    @GetMapping("/staffs")
    public String manageStaffs(Model model, Principal principal) {
        if (principal != null) model.addAttribute("username", principal.getName());
        List<User> staffs = userRepository.findAll().stream()
                .filter(u -> u.getRoles() != null && (u.getRoles().contains("STAFF") || u.getRoles().contains("DRIVER")))
                .collect(Collectors.toList());
        model.addAttribute("users", staffs);
        model.addAttribute("newUser", new User());
        model.addAttribute("pageType", "staff");
        return "admin/staffs";
    }

    // --- Shared Save/Delete Logic ---
    @PostMapping("/users/save")
    public String saveUser(
            @ModelAttribute User user,
            @RequestParam(required = false) String newPassword,
            @RequestParam String role,
            @RequestParam String pageType,
            RedirectAttributes redirectAttributes
    ) {
        String parsedUsername = user.getUsername().trim();
        
        if (user.getId() != null && user.getId().trim().isEmpty()) {
            user.getId();
            user.setId(null);
        }

        // Tạo mới
        if (user.getId() == null) {
            if (userRepository.findByUsername(parsedUsername).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Mã số/Username này đã tồn tại!");
                return pageType.equals("student") ? "redirect:/admin/students" : "redirect:/admin/staffs";
            }
        }

        // Lấy thông tin user cũ nếu chỉnh sửa
        User existingUser = null;
        if (user.getId() != null) {
            existingUser = userRepository.findById(user.getId()).orElse(null);
            // Kiểm tra đổi username sang mssv khác đã tồn tại chưa
            if (existingUser != null && !existingUser.getUsername().equals(parsedUsername)) {
                if (userRepository.findByUsername(parsedUsername).isPresent()) {
                    redirectAttributes.addFlashAttribute("error", "Mã số mới đã bị trùng!");
                    return pageType.equals("student") ? "redirect:/admin/students" : "redirect:/admin/staffs";
                }
            }
        }

        user.setUsername(parsedUsername);
        user.setFullName(user.getFullName().trim());

        // Xử lý Password
        if (existingUser != null) {
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                user.setPassword(passwordEncoder.encode(newPassword));
            } else {
                user.setPassword(existingUser.getPassword());
            }
        } else {
            if (newPassword == null || newPassword.trim().isEmpty()) {
                user.setPassword(passwordEncoder.encode("123456")); // Mật khẩu mặc định nếu quên nhập
            } else {
                user.setPassword(passwordEncoder.encode(newPassword));
            }
        }

        Set<String> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "Lưu thành công!");
        return pageType.equals("student") ? "redirect:/admin/students" : "redirect:/admin/staffs";
    }

    @PostMapping({"/users/delete/{id}", "/users/delete/", "/users/delete"})
    public String deleteUser(@PathVariable(required = false) String id, @RequestParam String pageType) {
        if (id == null) id = "";
        userRepository.deleteById(id);
        return pageType.equals("student") ? "redirect:/admin/students" : "redirect:/admin/staffs";
    }
}
