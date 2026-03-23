package com.example.HUTECHBUS.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

/**
 * Controller MVC - tra ve giao dien HTML cho Driver App.
 */
@Controller
public class DriverController {

    @GetMapping("/driver/app")
    public String driverApp(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        return "driver-app";
    }
}