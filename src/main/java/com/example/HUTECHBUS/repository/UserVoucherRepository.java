package com.example.HUTECHBUS.repository;

import com.example.HUTECHBUS.model.UserVoucher;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserVoucherRepository extends MongoRepository<UserVoucher, String> {
    List<UserVoucher> findByUserId(String userId);
    List<UserVoucher> findByUserIdAndStatus(String userId, String status);
}
