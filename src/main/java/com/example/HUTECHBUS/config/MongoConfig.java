package com.example.HUTECHBUS.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Cấu hình kết nối MongoDB.
 *
 * Mở rộng AbstractMongoClientConfiguration để đảm bảo ứng dụng
 * luôn kết nối đến đúng database "HUTECHBUS", tránh bị auto-configuration
 * chuyển sang database mặc định "test".
 */
@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    /** Tên database MongoDB được sử dụng */
    @Override
    protected String getDatabaseName() {
        return "HUTECHBUS";
    }

    /** Kết nối MongoDB trên localhost, cổng mặc định 27017 */
    @Override
    @Bean
    @Primary
    public MongoClient mongoClient() {
        return MongoClients.create("mongodb://localhost:27017");
    }

    /** MongoTemplate dùng cho các thao tác cơ sở dữ liệu tiên tiến */
    @Bean
    @Primary
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), getDatabaseName());
    }
}
