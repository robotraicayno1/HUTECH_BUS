package com.example.HUTECHBUS.config;

import com.example.HUTECHBUS.model.Route;
import com.example.HUTECHBUS.model.Stop;
import com.example.HUTECHBUS.model.User;
import com.example.HUTECHBUS.repository.RouteRepository;
import com.example.HUTECHBUS.repository.StopRepository;
import com.example.HUTECHBUS.repository.UserRepository;
import com.example.HUTECHBUS.repository.VoucherRepository;
import com.example.HUTECHBUS.model.Voucher;
import org.springframework.beans.factory.annotation.Autowired;
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
            PasswordEncoder passwordEncoder,
            VoucherRepository voucherRepository) {
        return args -> {
            // Xóa toàn bộ dữ liệu cũ để re-seed sạch
            userRepository.deleteAll();
            stopRepository.deleteAll();
            routeRepository.deleteAll();
            voucherRepository.deleteAll();

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
                    "STOP_LINHTRUNG", "STOP_LOTTE", "STOP_COCA", "STOP_E"));
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
            int[] samplePoints = { 500, 350, 200, 150, 100, 80, 60, 40, 20, 10 };
            for (int i = 1; i <= 10; i++) {
                User student = new User();
                student.setUsername(String.format("2111%02d", i));
                student.setPassword(passwordEncoder.encode("123456"));
                student.setFullName("Sinh viên HUTECH " + i);
                student.setRoles(Set.of("STUDENT"));
                student.setHPoints(samplePoints[i - 1]); // Điểm mẫu để test tính năng đổi điểm
                userRepository.save(student);
            }

            // ================================
            // VOUCHERS REDEEMABLE (MIXED PASSES & DISCOUNTS)
            // ================================
            Voucher singleTrip = new Voucher();
            singleTrip.setName("Vé Lẻ 1 Lượt (Miễn phí 1 chuyến)");
            singleTrip.setTicketType("SINGLE_TRIP");
            singleTrip.setPointCost(50);
            singleTrip.setDiscountAmount(0);
            voucherRepository.save(singleTrip);

            Voucher dailyVoucher = new Voucher();
            dailyVoucher.setName("Vé 1 Ngày (Miễn phí 24h)");
            dailyVoucher.setTicketType("DAILY");
            dailyVoucher.setPointCost(300);
            dailyVoucher.setDiscountAmount(0);
            voucherRepository.save(dailyVoucher);

            Voucher discount10kAny = new Voucher();
            discount10kAny.setName("Voucher Giảm 10.000đ (Áp dụng mọi vé)");
            discount10kAny.setTicketType("DISCOUNT");
            discount10kAny.setPointCost(800);
            discount10kAny.setDiscountAmount(10000);
            voucherRepository.save(discount10kAny);

            Voucher discount20kMonthly = new Voucher();
            discount20kMonthly.setName("Voucher Giảm 20.000đ (Vé Tháng)");
            discount20kMonthly.setTicketType("DISCOUNT");
            discount20kMonthly.setPointCost(1500);
            discount20kMonthly.setDiscountAmount(20000);
            voucherRepository.save(discount20kMonthly);

            Voucher discount100kYearly = new Voucher();
            discount100kYearly.setName("Voucher Giảm 100.000đ (Vé Năm)");
            discount100kYearly.setTicketType("DISCOUNT");
            discount100kYearly.setPointCost(5000);
            discount100kYearly.setDiscountAmount(100000);
            voucherRepository.save(discount100kYearly);

            // CÁC VOUCHER BỔ SUNG YÊU CẦU MỚI
            Voucher discount5kAny = new Voucher();
            discount5kAny.setName("Voucher Giảm 5.000đ (Áp dụng mọi vé)");
            discount5kAny.setTicketType("DISCOUNT");
            discount5kAny.setPointCost(400);
            discount5kAny.setDiscountAmount(5000);
            voucherRepository.save(discount5kAny);

            Voucher discount50kMonthly = new Voucher();
            discount50kMonthly.setName("Voucher Giảm 50.000đ (Vé Tháng)");
            discount50kMonthly.setTicketType("DISCOUNT");
            discount50kMonthly.setPointCost(3500);
            discount50kMonthly.setDiscountAmount(50000);
            voucherRepository.save(discount50kMonthly);

            Voucher cashback30k = new Voucher();
            cashback30k.setName("Voucher Hoàn Tiền 30.000đ (Áp dụng mọi vé)");
            cashback30k.setTicketType("DISCOUNT");
            cashback30k.setPointCost(2000);
            cashback30k.setDiscountAmount(30000);
            voucherRepository.save(cashback30k);

        };
    }
}
