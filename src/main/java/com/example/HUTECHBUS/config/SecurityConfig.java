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
 * - Tất cả các trang yêu cầu đăng nhập, ngoại trừ trang login và tài nguyên tĩnh.
 * - Xác thực người dùng từ MongoDB thông qua CustomUserDetailsService.
 * - Sau khi đăng nhập thành công, chuyển hướng đến /dashboard.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    /** BCrypt encoder dùng để mã hóa mật khẩu khi lưu và xác thực */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Cấu hình DaoAuthenticationProvider để dùng MongoDB */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /** Cấu hình chuỗi bộ lọc bảo mật */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
<<<<<<< HEAD
=======
            .authenticationProvider(authenticationProvider())
>>>>>>> MINHTAI
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/booking.html", "/index.html", "/css/**", "/js/**", "/images/**", "/api/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }
}
