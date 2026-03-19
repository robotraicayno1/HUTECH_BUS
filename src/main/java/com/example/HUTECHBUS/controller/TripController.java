package com.example.HUTECHBUS.controller;

import com.example.HUTECHBUS.model.Route;
import com.example.HUTECHBUS.model.TripHistory;
import com.example.HUTECHBUS.model.User;
import com.example.HUTECHBUS.repository.RouteRepository;
import com.example.HUTECHBUS.repository.TripHistoryRepository;
import com.example.HUTECHBUS.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Quản lý chức năng yêu thích tuyến xe và lịch sử chuyến đi của người dùng.
 */
@Controller
public class TripController {

    @Autowired private UserRepository userRepository;
    @Autowired private RouteRepository routeRepository;
    @Autowired private TripHistoryRepository tripHistoryRepository;

    /**
     * Hiển thị trang yêu thích.
     * Lấy danh sách tuyến xe đã lưu từ tài khoản người dùng hiện tại.
     */
    @GetMapping("/favorites")
    public String viewFavorites(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        List<Route> favoriteRoutes = new ArrayList<>();
        if (user != null && user.getFavoriteRouteIds() != null) {
            for (String id : user.getFavoriteRouteIds()) {
                routeRepository.findById(id).ifPresent(favoriteRoutes::add);
            }
        }

        model.addAttribute("username", principal.getName());
        model.addAttribute("routes", favoriteRoutes);
        return "favorites";
    }

    /**
     * Hiển thị trang lịch sử chuyến đi, sắp xếp theo thời gian mới nhất trước.
     */
    @GetMapping("/history")
    public String viewHistory(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        List<TripHistory> history = new ArrayList<>();
        if (user != null) {
            history = tripHistoryRepository.findByUserIdOrderByTripDateDesc(user.getId());
        }

        model.addAttribute("username", principal.getName());
        model.addAttribute("history", history);
        return "history";
    }

    /**
     * API để thêm hoặc xóa một tuyến xe khỏi danh sách yêu thích.
     * Nếu tuyến đã có trong danh sách → xóa. Nếu chưa có → thêm vào.
     *
     * @param routeId ID tuyến xe cần toggle
     * @return JSON chứa trạng thái mới: { success: true, isFavorite: true/false }
     */
    @PostMapping("/api/favorites/toggle/{routeId}")
    @ResponseBody
    public Map<String, Object> toggleFavorite(@PathVariable String routeId, Principal principal) {
        Map<String, Object> res = new HashMap<>();

        if (principal == null) {
            res.put("success", false);
            return res;
        }

        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null) {
            res.put("success", false);
            return res;
        }

        if (user.getFavoriteRouteIds() == null) {
            user.setFavoriteRouteIds(new HashSet<>());
        }

        boolean isFavorite;
        if (user.getFavoriteRouteIds().contains(routeId)) {
            user.getFavoriteRouteIds().remove(routeId);
            isFavorite = false;
        } else {
            user.getFavoriteRouteIds().add(routeId);
            isFavorite = true;
        }

        userRepository.save(user);
        res.put("success", true);
        res.put("isFavorite", isFavorite);
        return res;
    }

    /**
     * API để ghi nhận một chuyến đi mới vào lịch sử.
     * Tạo một bản ghi TripHistory với trạng thái COMPLETED.
     *
     * @param routeId ID tuyến xe đã sử dụng
     * @return JSON: { success: true/false }
     */
    @PostMapping("/api/trips/record/{routeId}")
    @ResponseBody
    public Map<String, Object> recordTrip(@PathVariable String routeId, Principal principal) {
        Map<String, Object> res = new HashMap<>();

        if (principal == null) {
            res.put("success", false);
            return res;
        }

        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        Route route = routeRepository.findById(routeId).orElse(null);

        if (user == null || route == null) {
            res.put("success", false);
            return res;
        }

        TripHistory trip = new TripHistory();
        trip.setUserId(user.getId());
        trip.setRouteId(route.getId());
        trip.setRouteName(route.getName());
        trip.setTripDate(LocalDateTime.now());
        trip.setStatus("COMPLETED");

        tripHistoryRepository.save(trip);
        res.put("success", true);
        return res;
    }
}
