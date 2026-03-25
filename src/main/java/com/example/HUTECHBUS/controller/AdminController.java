package com.example.HUTECHBUS.controller;

import com.example.HUTECHBUS.model.ActiveTrip;
import com.example.HUTECHBUS.model.Route;
import com.example.HUTECHBUS.model.Stop;
import com.example.HUTECHBUS.model.Vehicle;
import com.example.HUTECHBUS.model.Voucher;
import com.example.HUTECHBUS.repository.ActiveTripMongoRepository;
import com.example.HUTECHBUS.model.User;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.Principal;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private ActiveTripMongoRepository activeTripRepository;

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

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        
        java.util.List<Vehicle> vehicles = vehicleRepository.findAll();
        java.util.List<ActiveTrip> activeTrips = activeTripRepository.findByStatus("RUNNING");
        
        java.util.List<java.util.Map<String, Object>> vehicleInfos = new java.util.ArrayList<>();
        for (Vehicle v : vehicles) {
            java.util.Map<String, Object> info = new java.util.HashMap<>();
            info.put("id", v.getId());
            info.put("licensePlate", v.getLicensePlate());
            info.put("capacity", v.getCapacity());
            info.put("status", v.getStatus());
            info.put("routeId", v.getRouteId());
            info.put("driverId", v.getDriverId());
            info.put("assignedDriverName", v.getDriverName());
            
            // Tìm chuyến đang chạy của xe này
            java.util.Optional<ActiveTrip> activeTrip = activeTrips.stream()
                .filter(t -> v.getId().equals(t.getVehicleId()))
                .findFirst();
                
            if (activeTrip.isPresent()) {
                info.put("isActive", true);
                info.put("activeDriverName", activeTrip.get().getDriverName());
            } else {
                info.put("isActive", false);
                info.put("activeDriverName", null);
            }
            vehicleInfos.add(info);
        }
        
        model.addAttribute("vehicleInfos", vehicleInfos);
        model.addAttribute("vehicles", vehicles); // For modal th:object
        model.addAttribute("routes", routeRepository.findAll());
        model.addAttribute("drivers", userRepository.findByRolesContaining("MANAGER"));
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
        
        // Lookup driver name if driverId is provided
        if (vehicle.getDriverId() != null && !vehicle.getDriverId().isEmpty()) {
            userRepository.findByUsername(vehicle.getDriverId()).ifPresent(u -> {
                vehicle.setDriverName(u.getFullName());
            });
        } else {
            vehicle.setDriverName(null);
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

    // --- Students Management ---
    @GetMapping("/students")
    public String manageStudents(Model model, Principal principal) {
        if (principal != null) model.addAttribute("username", principal.getName());
        List<User> students = userRepository.findByRolesContaining("STUDENT");
        model.addAttribute("students", students);
        return "admin/students";
    }

    @PostMapping("/students/save")
    public String saveStudent(
            @RequestParam(required = false) String id,
            @RequestParam String fullName,
            @RequestParam String username,
            @RequestParam(required = false) String password) {

        User student;
        if (id != null && !id.trim().isEmpty()) {
            student = userRepository.findById(id).orElse(new User());
        } else {
            student = new User();
            student.setRoles(new HashSet<>(Set.of("STUDENT")));
        }

        student.setFullName(fullName);
        student.setUsername(username);
        
        // Only update password if provided
        if (password != null && !password.trim().isEmpty()) {
            student.setPassword(passwordEncoder.encode(password));
        } else if (student.getId() == null) {
            // Default password for new student if empty
            student.setPassword(passwordEncoder.encode("123456"));
        }

        userRepository.save(student);
        return "redirect:/admin/students";
    }

    @PostMapping({"/students/delete/{id}", "/students/delete/", "/students/delete"})
    public String deleteStudent(@PathVariable(required = false) String id) {
        if (id != null && !id.trim().isEmpty()) {
            userRepository.deleteById(id);
        }
        return "redirect:/admin/students";
    }

    // --- Staffs Management ---
    @GetMapping("/staffs")
    public String manageStaffs(Model model, Principal principal) {
        if (principal != null) model.addAttribute("username", principal.getName());
        List<User> allUsers = userRepository.findAll();
        // Filter users who are STAFF or DRIVER
        List<User> staffs = allUsers.stream()
                .filter(u -> u.getRoles() != null && 
                             (u.getRoles().contains("STAFF") || u.getRoles().contains("DRIVER")))
                .collect(Collectors.toList());
        model.addAttribute("staffs", staffs);
        return "admin/staffs";
    }

    @PostMapping("/staffs/save")
    public String saveStaff(
            @RequestParam(required = false) String id,
            @RequestParam String fullName,
            @RequestParam String username,
            @RequestParam(required = false) String password,
            @RequestParam String role) {

        User staff;
        if (id != null && !id.trim().isEmpty()) {
            staff = userRepository.findById(id).orElse(new User());
        } else {
            staff = new User();
        }

        staff.setFullName(fullName);
        staff.setUsername(username);
        
        // Update roles based on selection
        Set<String> roles = new HashSet<>();
        roles.add(role); // expecting "STAFF" or "DRIVER"
        staff.setRoles(roles);
        
        if (password != null && !password.trim().isEmpty()) {
            staff.setPassword(passwordEncoder.encode(password));
        } else if (staff.getId() == null) {
            staff.setPassword(passwordEncoder.encode("123456"));
        }

        userRepository.save(staff);
        return "redirect:/admin/staffs";
    }

    @PostMapping({"/staffs/delete/{id}", "/staffs/delete/", "/staffs/delete"})
    public String deleteStaff(@PathVariable(required = false) String id) {
        if (id != null && !id.trim().isEmpty()) {
            userRepository.deleteById(id);
        }
        return "redirect:/admin/staffs";
    }
}
