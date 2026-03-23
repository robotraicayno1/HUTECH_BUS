package com.example.HUTECHBUS;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Điểm khởi động chính của ứng dụng HUTECHBUS.
 * Ứng dụng quản lý tra cứu tuyến xe bus và theo dõi lịch trình cho sinh viên HUTECH.
 */
@SpringBootApplication
public class HutechbusApplication {

    public static void main(String[] args) {
        SpringApplication.run(HutechbusApplication.class, args);
    }
}
