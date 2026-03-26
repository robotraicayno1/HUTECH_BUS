package com.example.HUTECHBUS.controller;

import com.example.HUTECHBUS.model.Route;
import com.example.HUTECHBUS.model.Stop;
import com.example.HUTECHBUS.model.Vehicle;
import com.example.HUTECHBUS.model.Voucher;
import com.example.HUTECHBUS.repository.RouteRepository;
import com.example.HUTECHBUS.repository.StopRepository;
import com.example.HUTECHBUS.repository.TicketPassRepository;
import com.example.HUTECHBUS.repository.PassPackageRepository;
import com.example.HUTECHBUS.repository.TripHistoryRepository;
import com.example.HUTECHBUS.repository.UserRepository;
import com.example.HUTECHBUS.repository.UserVoucherRepository;
import com.example.HUTECHBUS.repository.VehicleRepository;
import com.example.HUTECHBUS.repository.VoucherRepository;
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

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private UserVoucherRepository userVoucherRepository;

    @Autowired
    private TicketPassRepository ticketPassRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PassPackageRepository passPackageRepository;

    // --- Dashboard ---
    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        model.addAttribute("vehicleCount", vehicleRepository.count());
        model.addAttribute("routeCount", routeRepository.count());
        model.addAttribute("stopCount", stopRepository.count());

        java.time.LocalDateTime thirtyDaysAgo = java.time.LocalDateTime.now().minusDays(30);
        java.util.List<com.example.HUTECHBUS.model.TripHistory> allHistory =
                tripHistoryRepository.findByTripDateAfter(thirtyDaysAgo);

        // --- 1. Tổng hành khách 30 ngày ---
        model.addAttribute("totalPassengers30d", allHistory.size());

        // --- 2. Mật độ sử dụng từng tuyến (30 ngày) ---
        java.util.Map<String, Long> routeDensity = new java.util.LinkedHashMap<>();
        for (com.example.HUTECHBUS.model.TripHistory h : allHistory) {
            String rName = h.getRouteName() != null ? h.getRouteName() : "Không rõ";
            routeDensity.merge(rName, 1L, Long::sum);
        }
        java.util.List<java.util.Map.Entry<String, Long>> routeEntries = new java.util.ArrayList<>(routeDensity.entrySet());
        routeEntries.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));
        model.addAttribute("routeLabels", routeEntries.stream().map(java.util.Map.Entry::getKey).toArray());
        model.addAttribute("routeValues", routeEntries.stream().map(java.util.Map.Entry::getValue).toArray());

        // --- 3. Biểu đồ lưu lượng 30 ngày (theo ngày) ---
        java.util.Map<java.time.LocalDate, Long> dailyTraffic = new java.util.TreeMap<>();
        for (int i = 29; i >= 0; i--) {
            dailyTraffic.put(java.time.LocalDate.now().minusDays(i), 0L);
        }
        for (com.example.HUTECHBUS.model.TripHistory h : allHistory) {
            java.time.LocalDate date = h.getTripDate().toLocalDate();
            if (dailyTraffic.containsKey(date)) {
                dailyTraffic.merge(date, 1L, Long::sum);
            }
        }
        model.addAttribute("chartLabels", dailyTraffic.keySet().stream()
                .map(d -> d.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM"))).toArray());
        model.addAttribute("chartValues", dailyTraffic.values().toArray());

        // --- 4. Lịch sử chuyến đi gần đây (7 ngày gần nhất, max 10 bản ghi cho dashboard) ---
        java.time.LocalDateTime sevenDaysAgo = java.time.LocalDateTime.now().minusDays(7);
        java.util.List<com.example.HUTECHBUS.model.TripHistory> recentHistory =
                tripHistoryRepository.findByTripDateAfter(sevenDaysAgo);
        recentHistory.sort((a, b) -> b.getTripDate().compareTo(a.getTripDate()));
        if (recentHistory.size() > 10) recentHistory = recentHistory.subList(0, 10);
        model.addAttribute("recentHistory", recentHistory);

        // --- 5. Tổng tất cả chuyến đi ---
        model.addAttribute("totalAllPassengers", tripHistoryRepository.count());

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

    // --- Vouchers Management ---
    @GetMapping("/vouchers")
    public String manageVouchers(Model model, Principal principal) {
        if (principal != null) model.addAttribute("username", principal.getName());
        model.addAttribute("vouchers", voucherRepository.findAll());
        java.util.List<com.example.HUTECHBUS.model.UserVoucher> userVouchers = userVoucherRepository.findAll();
        model.addAttribute("userVouchers", userVouchers);
        model.addAttribute("newVoucher", new Voucher());
        // Build userId -> username map for display
        java.util.Map<String, String> userMap = new java.util.HashMap<>();
        userRepository.findAll().forEach(u -> userMap.put(u.getId(), u.getUsername()));
        model.addAttribute("userMap", userMap);
        return "admin/vouchers";
    }

    @PostMapping("/vouchers/save")
    public String saveVoucher(@ModelAttribute Voucher voucher) {
        if (voucher.getId() != null && voucher.getId().trim().isEmpty()) {
            voucher.setId(null);
        }
        voucherRepository.save(voucher);
        return "redirect:/admin/vouchers";
    }

    @PostMapping({"/vouchers/delete/{id}", "/vouchers/delete/", "/vouchers/delete"})
    public String deleteVoucher(@PathVariable(required = false) String id) {
        if (id == null) id = "";
        voucherRepository.deleteById(id);
        return "redirect:/admin/vouchers";
    }

    // --- Buy-Pass Packages Management ---
    @GetMapping("/buy-pass")
    public String manageBuyPass(Model model, Principal principal) {
        if (principal != null) model.addAttribute("username", principal.getName());
        java.util.List<com.example.HUTECHBUS.model.PassPackage> packages = passPackageRepository.findAll();
        model.addAttribute("packages", packages);
        return "admin/buy-pass";
    }

    @PostMapping("/buy-pass/save")
    public String saveBuyPassPackage(
            @RequestParam(required = false) String id,
            @RequestParam String name,
            @RequestParam String type,
            @RequestParam long price,
            @RequestParam int durationDays,
            @RequestParam String description) {
        
        com.example.HUTECHBUS.model.PassPackage pkg;
        if (id != null && !id.trim().isEmpty()) {
            pkg = passPackageRepository.findById(id).orElse(new com.example.HUTECHBUS.model.PassPackage());
        } else {
            pkg = new com.example.HUTECHBUS.model.PassPackage();
        }
        
        pkg.setName(name);
        pkg.setType(type);
        pkg.setPrice(price);
        pkg.setDurationDays(durationDays);
        pkg.setDescription(description);
        
        passPackageRepository.save(pkg);
        return "redirect:/admin/buy-pass";
    }

    @PostMapping({"/buy-pass/delete/{id}", "/buy-pass/delete/", "/buy-pass/delete"})
    public String deleteBuyPassPackage(@PathVariable(required = false) String id) {
        if (id != null && !id.trim().isEmpty()) {
            passPackageRepository.deleteById(id);
        }
        return "redirect:/admin/buy-pass";
    }
}

