package com.example.HUTECHBUS.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

/**
 * Chuyển hướng người dùng về trang đúng sau khi đăng nhập thành công,
 * dựa trên vai trò (role) của họ.
 *
 * ADMIN   → /admin/dashboard
 * DRIVER  → /driver/app
 * USER    → /dashboard
 */
@Component
public class RoleBasedSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        String redirectUrl = "/dashboard"; // Mặc định cho STUDENT

        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();
            if ("ROLE_ADMIN".equals(role)) {
                redirectUrl = "/admin/dashboard";
                break;
            } else if ("ROLE_MANAGER".equals(role) || "ROLE_DRIVER".equals(role)) {
                redirectUrl = "/driver/app";
                break;
            }
        }

        response.sendRedirect(redirectUrl);
    }
}
