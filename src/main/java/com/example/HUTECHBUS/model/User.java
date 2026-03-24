package com.example.HUTECHBUS.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

/**
 * Đại diện cho một người dùng trong hệ thống.
 * Bao gồm sinh viên, tài xế và quản trị viên.
 *
 * Collection MongoDB: "Users"
 */
@Data
@Document(collection = "Users")
public class User {

    @Id
    private String id;

    /** Tên đăng nhập - thường là mã sinh viên (VD: 211101) */
    private String username;

    /** Mật khẩu đã được mã hóa bằng BCrypt */
    private String password;

    /** Họ và tên đầy đủ */
    private String fullName;

    /** Vai trò của người dùng: ADMIN, MANAGER, STUDENT */
    private Set<String> roles;

    /** Danh sách ID các tuyến xe yêu thích của người dùng */
    private Set<String> favoriteRouteIds = new HashSet<>();

    /** ID của thẻ vé định kỳ đang hoạt động (nếu có) */
    private String activePassId;
}
