package com.example.HUTECHBUS.repository;

import com.example.HUTECHBUS.model.Voucher;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoucherRepository extends MongoRepository<Voucher, String> {
    List<Voucher> findByTicketType(String ticketType);
}
