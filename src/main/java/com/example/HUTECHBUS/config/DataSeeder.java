package com.example.HUTECHBUS.config;

import com.example.HUTECHBUS.model.Route;
import com.example.HUTECHBUS.model.Stop;
import com.example.HUTECHBUS.model.User;
import com.example.HUTECHBUS.repository.RouteRepository;
import com.example.HUTECHBUS.repository.StopRepository;
import com.example.HUTECHBUS.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

/**
 * Khởi tạo dữ liệu mẫu cho cơ sở dữ liệu HUTECHBUS khi ứng dụng khởi động.
 *
 * Mỗi lần khởi động, toàn bộ dữ liệu cũ sẽ bị xóa và được tạo lại để đảm bảo
 * sự nhất quán giữa các phiên phát triển.
 *
 * Dữ liệu được tạo bao gồm:
 * - Các trạm dừng (Stops) của các tuyến
 * - Tuyến 01 và Tuyến 167
 * - Tài khoản admin, tài xế, và 10 sinh viên mẫu
 */
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository,
                                   StopRepository stopRepository,
                                   RouteRepository routeRepository,
                                   PasswordEncoder passwordEncoder) {
        return args -> {
            // Xóa toàn bộ dữ liệu cũ để re-seed sạch
            userRepository.deleteAll();
            stopRepository.deleteAll();
            routeRepository.deleteAll();

            // ================================
            // CÁC TRẠM DỪNG - CAMPUS HUTECH
            // ================================

            Stop campusA = new Stop();
            campusA.setId("STOP_A");
            campusA.setName("HUTECH Campus A (Điện Biên Phủ)");
            campusA.setLatitude(10.8018);
            campusA.setLongitude(106.7115);
            campusA.setDescription("Cơ sở chính của HUTECH trên đường Điện Biên Phủ");
            stopRepository.save(campusA);

            Stop campusB = new Stop();
            campusB.setId("STOP_B");
            campusB.setName("HUTECH Campus B (Ung Văn Khiêm)");
            campusB.setLatitude(10.8055);
            campusB.setLongitude(106.7135);
            campusB.setDescription("Cơ sở B của HUTECH trên đường Ung Văn Khiêm");
            stopRepository.save(campusB);

            Stop campusE = new Stop();
            campusE.setId("STOP_E");
            campusE.setName("HUTECH Campus E (SHTP - Q9)");
            campusE.setLatitude(10.8436);
            campusE.setLongitude(106.7944);
            campusE.setDescription("Cơ sở E của HUTECH tại Khu Công nghệ cao, Quận 9");
            stopRepository.save(campusE);

            // ================================
            // CÁC TRẠM DỪNG - TUYẾN 167
            // ================================

            Stop nongLam = new Stop();
            nongLam.setId("STOP_NONGLAM");
            nongLam.setName("Đại học Nông Lâm");
            nongLam.setLatitude(10.8718);
            nongLam.setLongitude(106.7915);
            stopRepository.save(nongLam);

            Stop linhTrung = new Stop();
            linhTrung.setId("STOP_LINHTRUNG");
            linhTrung.setName("KCX Linh Trung 1");
            linhTrung.setLatitude(10.8655);
            linhTrung.setLongitude(106.7845);
            stopRepository.save(linhTrung);

            Stop gaMetroCNC = new Stop();
            gaMetroCNC.setId("STOP_METRO_CNC");
            gaMetroCNC.setName("Ga Metro Khu Công nghệ cao");
            gaMetroCNC.setLatitude(10.8458);
            gaMetroCNC.setLongitude(106.7941);
            stopRepository.save(gaMetroCNC);

            Stop dhKinhTeLuat = new Stop();
            dhKinhTeLuat.setId("STOP_UEL");
            dhKinhTeLuat.setName("ĐH Kinh tế Luật");
            dhKinhTeLuat.setLatitude(10.8711);
            dhKinhTeLuat.setLongitude(106.7788);
            stopRepository.save(dhKinhTeLuat);

            Stop thptDaoSonTay = new Stop();
            thptDaoSonTay.setId("STOP_DAOSONTAY");
            thptDaoSonTay.setName("THPT Đào Sơn Tây");
            thptDaoSonTay.setLatitude(10.8790);
            thptDaoSonTay.setLongitude(106.7650);
            stopRepository.save(thptDaoSonTay);

            Stop goDiAn = new Stop();
            goDiAn.setId("STOP_GODIAN");
            goDiAn.setName("GO! Dĩ An (QL 1K)");
            goDiAn.setLatitude(10.8950);
            goDiAn.setLongitude(106.7730);
            stopRepository.save(goDiAn);

            Stop bvHoanMy = new Stop();
            bvHoanMy.setId("STOP_HOANMY");
            bvHoanMy.setName("Bệnh viện Hoàn Mỹ");
            bvHoanMy.setLatitude(10.8900);
            bvHoanMy.setLongitude(106.7700);
            stopRepository.save(bvHoanMy);

            Stop ttHanhChinhDiAn = new Stop();
            ttHanhChinhDiAn.setId("STOP_HCDIAN");
            ttHanhChinhDiAn.setName("TT Hành chính Dĩ An");
            ttHanhChinhDiAn.setLatitude(10.9080);
            ttHanhChinhDiAn.setLongitude(106.7710);
            stopRepository.save(ttHanhChinhDiAn);

            Stop nguyenTriPhuong = new Stop();
            nguyenTriPhuong.setId("STOP_NTPHUONG");
            nguyenTriPhuong.setName("Đường Nguyễn Tri Phương");
            nguyenTriPhuong.setLatitude(10.8980);
            nguyenTriPhuong.setLongitude(106.7820);
            stopRepository.save(nguyenTriPhuong);

            Stop nhaThoXuanHiep = new Stop();
            nhaThoXuanHiep.setId("STOP_XUANHIEP");
            nhaThoXuanHiep.setName("Nhà thờ Xuân Hiệp");
            nhaThoXuanHiep.setLatitude(10.8780);
            nhaThoXuanHiep.setLongitude(106.7820);
            stopRepository.save(nhaThoXuanHiep);

            Stop lotteCinema = new Stop();
            lotteCinema.setId("STOP_LOTTE");
            lotteCinema.setName("Lotte Cinema Thủ Đức");
            lotteCinema.setLatitude(10.8550);
            lotteCinema.setLongitude(106.7750);
            stopRepository.save(lotteCinema);

            Stop cocacola = new Stop();
            cocacola.setId("STOP_COCA");
            cocacola.setName("Công ty CocaCola");
            cocacola.setLatitude(10.8520);
            cocacola.setLongitude(106.7700);
            stopRepository.save(cocacola);

            // ================================
            // TUYẾN XE BUS
            // ================================

            // Tuyến 01: Kết nối Campus A, B và Campus E (SHTP)
            Route route1 = new Route();
            route1.setId("ROUTE_01");
            route1.setName("Tuyến 01: Campus A - Campus E");
            route1.setDescription("Tuyến bus kết nối cơ sở chính và khu công nghệ cao");
            route1.setStopIds(List.of("STOP_A", "STOP_B", "STOP_E"));
            route1.setColorCode("#ff6b00");
            routeRepository.save(route1);

            // Tuyến 167: ĐH Nông Lâm → HUTECH E → Di An → KCX Linh Trung
            Route route167 = new Route();
            route167.setId("ROUTE_167");
            route167.setName("Tuyến 167: ĐH Nông Lâm - HUTECH Campus E - Di An");
            route167.setDescription("Lộ trình: HUTECH E → Metro → ĐH Nông Lâm → UEL → Dĩ An → Linh Trung");
            route167.setStopIds(List.of(
                "STOP_E", "STOP_METRO_CNC", "STOP_NONGLAM", "STOP_UEL",
                "STOP_DAOSONTAY", "STOP_GODIAN", "STOP_HOANMY",
                "STOP_HCDIAN", "STOP_NTPHUONG", "STOP_XUANHIEP",
                "STOP_LINHTRUNG", "STOP_LOTTE", "STOP_COCA", "STOP_E"
            ));
            route167.setColorCode("#003366");
            routeRepository.save(route167);

            // ================================
            // TÀI KHOẢN NGƯỜI DÙNG MẪU
            // ================================

            // Quản trị viên
            User admin = new User();
            admin.setUsername("admin01");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("Quản trị viên");
            admin.setRoles(Set.of("ADMIN"));
            userRepository.save(admin);

            // Tài xế / Quản lý tuyến
            User driver = new User();
            driver.setUsername("driver01");
            driver.setPassword(passwordEncoder.encode("driver123"));
            driver.setFullName("Tài xế Nguyễn Văn Tải");
            driver.setRoles(Set.of("MANAGER"));
            userRepository.save(driver);

            // 10 Sinh viên mẫu (211101 → 211110, mật khẩu: 123456)
            for (int i = 1; i <= 10; i++) {
                User student = new User();
                student.setUsername(String.format("2111%02d", i));
                student.setPassword(passwordEncoder.encode("123456"));
                student.setFullName("Sinh viên HUTECH " + i);
                student.setRoles(Set.of("STUDENT"));
                userRepository.save(student);
            }
        };
    }
}
