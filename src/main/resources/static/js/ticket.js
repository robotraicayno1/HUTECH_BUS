// --- Logic khởi tạo dữ liệu Sinh QR Code ---
// Đảm bảo DOM parse xong mới chạy
document.addEventListener('DOMContentLoaded', () => {
    
    // (1) Đọc cấu hình ghế đã mua từ URL Parameter hoặc LocalStorage (do trang booking lưu vào)
    // Trong môi trường thực tế, cái này sẽ được fetch JSON từ Server qua REST API do backend trả về.
    const bookedSeatsJSON = localStorage.getItem('hutech_booked_seats');
    let bookedSeats = [];
    
    if (bookedSeatsJSON) {
        bookedSeats = JSON.parse(bookedSeatsJSON);
    } else {
        // Fallback test data
        bookedSeats = ['1A', '2B']; 
    }
    
    // (2) Hiển thị thông tin lên giao diện chữ
    document.getElementById('ticket-seats').textContent = bookedSeats.join(', ');
    
    const today = new Date();
    const formattedDate = today.toLocaleDateString('vi-VN');
    document.getElementById('ticket-date').textContent = formattedDate;

    // (3) Thiết kế cấu trúc chuỗi Payload bảo mật mã hóa chứa trong QR
    // Thông thường gồm: Mã Hóa Đơn + Mã Sinh Viên + Thời gian
    // Ở demo này ta lấy timestamp và danh sách ghế để mix lại
    const timestamp = Date.now();
    const qrDataPayload = `HUTECHBUS|SV:NguyenVanA|GH_DAT:${bookedSeats.join(',')}|TX:${timestamp}`;
    
    console.log("Dữ liệu ẩn trong QR:", qrDataPayload);

    // (4) Render QR Code bằng thư viện qrcode.js vào thẻ div #qrcode
    // Tham khảo API: https://davidshimjs.github.io/qrcodejs/
    const qrElement = document.getElementById("qrcode");
    
    // Xóa nội dung cũ trước khi sinh mới (nếu có)
    qrElement.innerHTML = '';
    
    // Khởi tạo Object
    new QRCode(qrElement, {
        text: qrDataPayload,
        width: 160,       // Chiều rộng pixel
        height: 160,      // Chiều cao pixel
        colorDark : "#000000",   // Màu QR (Đen để tăng độ tương phản camera dễ quyét)
        colorLight : "#ffffff",  // Màu nền QR 
        correctLevel : QRCode.CorrectLevel.H // Mức độ chống lỗi H (Khôi phục được 30% nếu mờ)
    });

    // (5) Logic đếm ngược 30 phút cho trang vé
    let timer = 30 * 60;
    const display = document.getElementById('countdown-timer');
    if (display) {
        const countdownInterval = setInterval(function () {
            let minutes = parseInt(timer / 60, 10);
            let seconds = parseInt(timer % 60, 10);
            
            minutes = minutes < 10 ? "0" + minutes : minutes;
            seconds = seconds < 10 ? "0" + seconds : seconds;
            
            display.textContent = minutes + ":" + seconds;
            
            if (--timer < 0) {
                clearInterval(countdownInterval);
                display.textContent = "00:00";
                alert('Mã QR đã hết hạn hiệu lực!');
                // Làm mờ QR khi hết hạn
                document.querySelector('.qr-container').style.opacity = '0.2';
            }
        }, 1000);
    }
});
