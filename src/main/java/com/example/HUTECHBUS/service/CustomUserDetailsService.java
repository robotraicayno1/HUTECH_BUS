package com.example.HUTECHBUS.service;

import com.example.HUTECHBUS.model.User;
import com.example.HUTECHBUS.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Tích hợp với Spring Security để tải thông tin người dùng từ MongoDB
 * phục vụ cho quá trình xác thực (authentication).
 *
 * Được gọi tự động bởi Spring Security khi người dùng đăng nhập.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Tải thông tin người dùng dựa vào tên đăng nhập.
     * Chuyển đổi các vai trò (roles) thành GrantedAuthority với tiền tố "ROLE_".
     *
     * @param username Tên đăng nhập (mã sinh viên)
     * @return UserDetails dùng cho Spring Security
     * @throws UsernameNotFoundException Nếu không tìm thấy người dùng
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getRoles() != null
                        ? user.getRoles().stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                .collect(Collectors.toList())
                        : Collections.emptyList()
        );
    }
}
