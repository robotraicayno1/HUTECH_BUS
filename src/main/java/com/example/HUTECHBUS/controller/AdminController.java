package com.example.HUTECHBUS.controller;

import com.example.HUTECHBUS.model.Route;
import com.example.HUTECHBUS.model.Stop;
import com.example.HUTECHBUS.model.Vehicle;
import com.example.HUTECHBUS.repository.RouteRepository;
import com.example.HUTECHBUS.repository.StopRepository;
import com.example.HUTECHBUS.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private StopRepository stopRepository;

    // --- Dashboard ---
    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        model.addAttribute("vehicleCount", vehicleRepository.count());
        model.addAttribute("routeCount", routeRepository.count());
        model.addAttribute("stopCount", stopRepository.count());
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
