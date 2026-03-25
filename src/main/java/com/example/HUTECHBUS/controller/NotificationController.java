package com.example.HUTECHBUS.controller;

import com.example.HUTECHBUS.model.Notification;
import com.example.HUTECHBUS.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping("/notifications")
    public String viewNotifications(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        String username = principal.getName();
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(username);
        long unreadCount = notifications.stream().filter(n -> !n.isRead()).count();

        model.addAttribute("username", username);
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);
        return "notifications";
    }

    @PostMapping("/api/notifications/{id}/read")
    @ResponseBody
    public Map<String, Object> markAsRead(@PathVariable String id) {
        Map<String, Object> res = new HashMap<>();
        notificationRepository.findById(id).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
        res.put("success", true);
        return res;
    }

    @PostMapping("/api/notifications/read-all")
    @ResponseBody
    public Map<String, Object> markAllRead(Principal principal) {
        Map<String, Object> res = new HashMap<>();
        if (principal == null) { res.put("success", false); return res; }
        List<Notification> unread = notificationRepository.findByUserIdAndReadFalse(principal.getName());
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
        res.put("success", true);
        return res;
    }
}
