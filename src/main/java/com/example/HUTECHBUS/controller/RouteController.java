package com.example.HUTECHBUS.controller;

import com.example.HUTECHBUS.model.Route;
import com.example.HUTECHBUS.model.Stop;
import com.example.HUTECHBUS.repository.RouteRepository;
import com.example.HUTECHBUS.repository.StopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Xử lý các tính năng tra cứu lộ trình và cung cấp dữ liệu API cho bản đồ.
 */
@Controller
public class RouteController {

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private StopRepository stopRepository;

    /**
     * Hiển thị trang tra cứu lộ trình với danh sách tất cả tuyến xe.
     */
    @GetMapping("/routes")
    public String viewRoutes(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        model.addAttribute("routes", routeRepository.findAll());
        return "routes";
    }

    /**
     * API endpoint trả về chi tiết một tuyến xe kèm theo danh sách trạm dừng theo thứ tự.
     * Dùng để vẽ lộ trình trên bản đồ Leaflet + OSRM.
     *
     * @param id ID của tuyến xe cần lấy thông tin
     * @return Map chứa thông tin route và danh sách stops
     */
    @GetMapping({"/api/routes/{id}/details", "/api/routes/{id}"})
    @ResponseBody
    public Map<String, Object> getRouteDetails(@PathVariable String id) {
        Route route = routeRepository.findById(id).orElse(null);
        if (route == null) return Map.of();

        List<Stop> stops = new ArrayList<>();
        if (route.getStopIds() != null) {
            for (String stopId : route.getStopIds()) {
                stopRepository.findById(stopId).ifPresent(stops::add);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("route", route);
        response.put("stops", stops);
        return response;
    }
}
