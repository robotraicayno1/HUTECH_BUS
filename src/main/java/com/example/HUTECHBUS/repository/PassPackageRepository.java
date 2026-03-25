package com.example.HUTECHBUS.repository;

import com.example.HUTECHBUS.model.PassPackage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PassPackageRepository extends MongoRepository<PassPackage, String> {
    Optional<PassPackage> findByType(String type);
}
