package com.example.HUTECHBUS.config;

import com.example.HUTECHBUS.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Cấu hình Spring Security cho ứng dụng HUTECHBUS.
 *
 * Ma trận quyền hạn (theo roles thực tế trong MongoDB):
 * - ADMIN   : /admin/**
 * - MANAGER : /driver/** (tài xế)
 * - STUDENT : /dashboard, /booking, và các trang người dùng
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private RoleBasedSuccessHandler roleBasedSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

<<<<<<< HEAD
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
=======

>>>>>>> bed3500435ebea4b61c6dcf60486cd4f095c2c85

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider())
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
<<<<<<< HEAD
                // Tài nguyên công khai
                .requestMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll()
                // API
                .requestMatchers("/api/**").permitAll()
                // Trang Admin: chỉ ADMIN
=======
                .requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**").permitAll()
>>>>>>> bed3500435ebea4b61c6dcf60486cd4f095c2c85
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Trang Tài xế: chỉ MANAGER hoặc DRIVER
                .requestMatchers("/driver/**").hasAnyRole("MANAGER", "DRIVER")
                // Trang người dùng: STUDENT, ADMIN và STAFF
                .requestMatchers("/dashboard", "/booking", "/ticket", "/routes",
                                 "/history", "/favorites", "/notifications",
                                 "/points", "/my-tickets", "/buy-pass").hasAnyRole("STUDENT", "ADMIN", "STAFF")
                // Mọi request còn lại phải đăng nhập
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
<<<<<<< HEAD
                .successHandler(roleBasedSuccessHandler)  // Chuyển hướng theo role
=======
                .successHandler((request, response, authentication) -> {
                    boolean isAdmin = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                    if (isAdmin) {
                        response.sendRedirect("/admin/dashboard");
                    } else {
                        response.sendRedirect("/dashboard");
                    }
                })
>>>>>>> bed3500435ebea4b61c6dcf60486cd4f095c2c85
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }
}
