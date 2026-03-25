package com.example.HUTECHBUS.repository;

import com.example.HUTECHBUS.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository thao tác với collection "Users" trong MongoDB.
 */
public interface UserRepository extends MongoRepository<User, String> {


    /** Tìm người dùng theo tên đăng nhập (mã sinh viên) */
    Optional<User> findByUsername(String username);

    /** Tìm danh sách người dùng có chứa một role cụ thể */
    List<User> findByRolesContaining(String role);
}
