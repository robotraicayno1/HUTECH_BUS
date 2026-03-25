package com.example.HUTECHBUS.controller;

import com.example.HUTECHBUS.model.User;
import com.example.HUTECHBUS.model.UserVoucher;
import com.example.HUTECHBUS.model.Voucher;
import com.example.HUTECHBUS.repository.UserRepository;
import com.example.HUTECHBUS.repository.UserVoucherRepository;
import com.example.HUTECHBUS.repository.VoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
public class PointController {

    @Autowired private UserRepository userRepository;
    @Autowired private VoucherRepository voucherRepository;
    @Autowired private UserVoucherRepository userVoucherRepository;

    @GetMapping("/points")
    public String viewPointsAndVouchers(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        List<Voucher> availableVouchers = voucherRepository.findAll();
        List<UserVoucher> myVouchers = userVoucherRepository.findByUserId(user.getId());

        model.addAttribute("username", principal.getName());
        model.addAttribute("hPoints", user.getHPoints());
        model.addAttribute("vouchers", availableVouchers);
        model.addAttribute("myVouchers", myVouchers);

        return "points";
    }

    @PostMapping("/api/points/exchange/{voucherId}")
    @ResponseBody
    public Map<String, Object> exchangeVoucher(@PathVariable String voucherId, Principal principal) {
        Map<String, Object> res = new HashMap<>();
        if (principal == null) {
            res.put("success", false);
            res.put("message", "Vui lòng đăng nhập.");
            return res;
        }

        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        Voucher voucher = voucherRepository.findById(voucherId).orElse(null);

        if (user == null || voucher == null) {
            res.put("success", false);
            res.put("message", "Dữ liệu không hợp lệ.");
            return res;
        }

        if (user.getHPoints() < voucher.getPointCost()) {
            res.put("success", false);
            res.put("message", "Bạn không đủ điểm để đổi voucher này.");
            return res;
        }

        // Trừ điểm
        user.setHPoints(user.getHPoints() - voucher.getPointCost());
        userRepository.save(user);

        // Tạo UserVoucher
        UserVoucher userVoucher = new UserVoucher();
        userVoucher.setUserId(user.getId());
        userVoucher.setVoucherId(voucher.getId());
        userVoucher.setVoucherName(voucher.getName());
        userVoucher.setTicketType(voucher.getTicketType());
        userVoucher.setDiscountAmount(voucher.getDiscountAmount());
        userVoucher.setStatus("ACTIVE");
        userVoucher.setAcquiredDate(LocalDateTime.now());

        userVoucherRepository.save(userVoucher);

        res.put("success", true);
        res.put("message", "Đổi voucher thành công!");
        res.put("newPoints", user.getHPoints());
        return res;
    }

    @PostMapping("/api/points/promo")
    @ResponseBody
    public Map<String, Object> applyPromo(@RequestBody Map<String, String> payload, Principal principal) {
        Map<String, Object> res = new HashMap<>();
        if (principal == null) {
            res.put("success", false);
            res.put("message", "Vui lòng đăng nhập.");
            return res;
        }

        String code = payload.get("code");
        if (code == null || code.trim().isEmpty()) {
            res.put("success", false);
            res.put("message", "Mã khuyến mãi không hợp lệ.");
            return res;
        }

        String upperCode = code.trim().toUpperCase();

        // Danh sách các mã khuyến mãi có sẵn
        Map<String, Integer> promoCodes = new HashMap<>();
        promoCodes.put("HUTECH2026", 500);
        promoCodes.put("TAN-SINH-VIEN", 1000);
        promoCodes.put("HUTECH-IT", 2000);
        promoCodes.put("MUNG-XUAN", 800);
        promoCodes.put("DI-BUS-NHIEU-HON", 300);

        if (promoCodes.containsKey(upperCode)) {
            User user = userRepository.findByUsername(principal.getName()).orElse(null);
            if (user != null) {
                if (user.getUsedPromoCodes() != null && user.getUsedPromoCodes().contains(upperCode)) {
                    res.put("success", false);
                    res.put("message", "Mã '" + upperCode + "' đã được sử dụng cho tài khoản của bạn rồi.");
                    return res;
                }
                
                int bonus = promoCodes.get(upperCode);
                user.setHPoints(user.getHPoints() + bonus);
                if (user.getUsedPromoCodes() == null) {
                    user.setUsedPromoCodes(new HashSet<>());
                }
                user.getUsedPromoCodes().add(upperCode);
                userRepository.save(user);

                res.put("success", true);
                res.put("message", "Áp dụng mã '" + upperCode + "' thành công! Bạn được thưởng +" + bonus + " H-Point.");
                res.put("newPoints", user.getHPoints());
                return res;
            }
        }

        res.put("success", false);
        res.put("message", "Mã khuyến mãi không chính xác hoặc đã hết hạn.");
        return res;
    }
}
