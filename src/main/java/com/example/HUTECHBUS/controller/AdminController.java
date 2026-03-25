package com.example.HUTECHBUS.controller;

import com.example.HUTECHBUS.model.Route;
import com.example.HUTECHBUS.model.Stop;
import com.example.HUTECHBUS.model.Vehicle;
import com.example.HUTECHBUS.repository.RouteRepository;
import com.example.HUTECHBUS.repository.StopRepository;
import com.example.HUTECHBUS.repository.TripHistoryRepository;
import com.example.HUTECHBUS.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private StopRepository stopRepository;

    @Autowired
    private TripHistoryRepository tripHistoryRepository;

    // --- Dashboard ---
    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        model.addAttribute("vehicleCount", vehicleRepository.count());
        model.addAttribute("routeCount", routeRepository.count());
        model.addAttribute("stopCount", stopRepository.count());

        // --- DỮ LIỆU BIỂU ĐỒ (7 ngày gần nhất) ---
        java.time.LocalDateTime sevenDaysAgo = java.time.LocalDateTime.now().minusDays(7);
        java.util.List<com.example.HUTECHBUS.model.TripHistory> history = tripHistoryRepository.findByTripDateAfter(sevenDaysAgo);

        // Nhóm theo ngày (yyyy-MM-dd)
        java.util.Map<java.time.LocalDate, Long> trafficData = new java.util.TreeMap<>();
        // Khởi tạo 7 ngày với giá trị 0
        for (int i = 0; i < 7; i++) {
            trafficData.put(java.time.LocalDate.now().minusDays(i), 0L);
        }

        for (com.example.HUTECHBUS.model.TripHistory h : history) {
            java.time.LocalDate date = h.getTripDate().toLocalDate();
            if (trafficData.containsKey(date)) {
                trafficData.put(date, trafficData.get(date) + 1);
            }
        }

        model.addAttribute("chartLabels", trafficData.keySet().stream().map(d -> d.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM"))).toArray());
        model.addAttribute("chartValues", trafficData.values().toArray());

        return "admin/dashboard";
    }

    // --- Vehicles Management ---
    @GetMapping("/vehicles")
    public String manageVehicles(Model model, Principal principal) {
        if (principal != null) model.addAttribute("username", principal.getName());
        model.addAttribute("vehicles", vehicleRepository.findAll());
        model.addAttribute("routes", routeRepository.findAll()); // For selecting route in form
        model.addAttribute("newVehicle", new Vehicle());
        return "admin/vehicles";
    }

    @PostMapping("/vehicles/save")
    public String saveVehicle(@ModelAttribute Vehicle vehicle) {
        if (vehicle.getId() != null && vehicle.getId().trim().isEmpty()) {
            vehicle.setId(null);
        }
        // Also handle "routeId" empty value if they selected "Chưa phân công"
        if (vehicle.getRouteId() != null && vehicle.getRouteId().trim().isEmpty()) {
            vehicle.setRouteId(null);
        }
        vehicleRepository.save(vehicle);
        return "redirect:/admin/vehicles";
    }

    @PostMapping({"/vehicles/delete/{id}", "/vehicles/delete/", "/vehicles/delete"})
    public String deleteVehicle(@PathVariable(required = false) String id) {
        if (id == null) id = "";
        vehicleRepository.deleteById(id);
        return "redirect:/admin/vehicles";
    }

    // --- Routes Management ---
    @GetMapping("/routes")
    public String manageRoutes(Model model, Principal principal) {
        if (principal != null) model.addAttribute("username", principal.getName());
        model.addAttribute("routes", routeRepository.findAll());
        model.addAttribute("stops", stopRepository.findAll()); // For selecting stops
        model.addAttribute("newRoute", new Route());
        return "admin/routes";
    }

    @PostMapping("/routes/save")
    public String saveRoute(@ModelAttribute Route route) {
        if (route.getId() != null && route.getId().trim().isEmpty()) {
            route.setId(null);
        }
        routeRepository.save(route);
        return "redirect:/admin/routes";
    }

    @PostMapping({"/routes/delete/{id}", "/routes/delete/", "/routes/delete"})
    public String deleteRoute(@PathVariable(required = false) String id) {
        if (id == null) id = "";
        routeRepository.deleteById(id);
        return "redirect:/admin/routes";
    }

    // --- Stops Management ---
    @GetMapping("/stops")
    public String manageStops(Model model, Principal principal) {
        if (principal != null) model.addAttribute("username", principal.getName());
        model.addAttribute("stops", stopRepository.findAll());
        model.addAttribute("newStop", new Stop());
        return "admin/stops";
    }

    @PostMapping("/stops/save")
    public String saveStop(@ModelAttribute Stop stop) {
        if (stop.getId() != null && stop.getId().trim().isEmpty()) {
            stop.setId(null);
        }
        stopRepository.save(stop);
        return "redirect:/admin/stops";
    }

    @PostMapping({"/stops/delete/{id}", "/stops/delete/", "/stops/delete"})
    public String deleteStop(@PathVariable(required = false) String id) {
        if (id == null) id = "";
        stopRepository.deleteById(id);
        return "redirect:/admin/stops";
    }
}
