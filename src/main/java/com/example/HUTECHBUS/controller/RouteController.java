package com.example.HUTECHBUS.controller;

import com.example.HUTECHBUS.model.ActiveTrip;
import com.example.HUTECHBUS.model.Route;
import com.example.HUTECHBUS.model.Stop;
import com.example.HUTECHBUS.model.Vehicle;
import com.example.HUTECHBUS.repository.VehicleRepository;
import com.example.HUTECHBUS.repository.ActiveTripMongoRepository;
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
import java.util.Optional;

/**
 * Xử lý các tính năng tra cứu lộ trình và cung cấp dữ liệu API cho bản đồ.
 */
@Controller
public class RouteController {

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private StopRepository stopRepository;

    @Autowired
    private ActiveTripMongoRepository activeTripRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    /**
     * Hiển thị trang tra cứu lộ trình với danh sách tất cả tuyến xe.
     */
    @GetMapping("/routes")
    public String viewRoutes(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        
        List<Route> routes = routeRepository.findAll();
        List<Map<String, Object>> routeData = new ArrayList<>();
        for (Route r : routes) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", r.getId());
            data.put("name", r.getName());
            data.put("description", r.getDescription());
            data.put("colorCode", r.getColorCode());

            // 1. Lấy tất cả xe được phân công cho tuyến này trong CSDL
            List<Vehicle> assignedVehicles = vehicleRepository.findByRouteId(r.getId());
            
            // 2. Lấy tất cả chuyến đang chạy của tuyến này
            List<ActiveTrip> activeTrips = activeTripRepository.findByRouteIdAndStatus(r.getId(), "RUNNING");
            
            List<Map<String, Object>> busOptions = new ArrayList<>();
            
            for (Vehicle v : assignedVehicles) {
                Map<String, Object> busInfo = new HashMap<>();
                busInfo.put("vehicleId", v.getId());
                busInfo.put("licensePlate", v.getLicensePlate());
                busInfo.put("capacity", v.getCapacity());
                busInfo.put("vehicleStatus", v.getStatus()); // Hoạt động, Bảo trì, v.v.
                
                // Tìm xem xe này có chuyến nào đang chạy không
                Optional<ActiveTrip> tripOpt = activeTrips.stream()
                        .filter(t -> v.getId().equals(t.getVehicleId()))
                        .findFirst();
                
                if (tripOpt.isPresent()) {
                    ActiveTrip trip = tripOpt.get();
                    int total = trip.getTotalSeats();
                    int taken = (trip.getLockedSeats() != null ? trip.getLockedSeats().size() : 0) +
                                (trip.getTransferPaidSeats() != null ? trip.getTransferPaidSeats().size() : 0) +
                                (trip.getOnlinePaidSeats() != null ? trip.getOnlinePaidSeats().size() : 0) +
                                (trip.getOnlineUnpaidSeats() != null ? trip.getOnlineUnpaidSeats().size() : 0);
                    
                    busInfo.put("hasActiveTrip", true);
                    busInfo.put("tripId", trip.getId() != null ? trip.getId() : "");
                    busInfo.put("availableSeats", total - taken);
                    busInfo.put("totalSeats", total);
                    String dName = (trip.getDriverName() != null && !trip.getDriverName().isEmpty()) 
                                   ? trip.getDriverName() 
                                   : (v.getDriverName() != null ? v.getDriverName() : "Tài xế HUTECH");
                    busInfo.put("driverName", dName);
                } else {
                    busInfo.put("hasActiveTrip", false);
                    busInfo.put("tripId", ""); // Đảm bảo không null cho Thymeleaf
                    busInfo.put("availableSeats", v.getCapacity());
                    busInfo.put("totalSeats", v.getCapacity());
                    busInfo.put("driverName", v.getDriverName() != null ? v.getDriverName() : "Sẵn sàng");
                }
                busOptions.add(busInfo);
            }
            
            data.put("hasActiveTrip", activeTrips.size() > 0);
            data.put("busOptions", busOptions); // Dùng tên mới để tránh nhầm lẫn
            
            // Backward compatibility for old UI if needed
            if (!busOptions.isEmpty()) {
                Map<String, Object> first = busOptions.get(0);
                data.put("availableSeats", first.get("availableSeats"));
                data.put("totalSeats", first.get("totalSeats"));
                data.put("vehicleLicensePlate", first.get("licensePlate"));
            }
            
            routeData.add(data);
        }

        model.addAttribute("routes", routeData);
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
