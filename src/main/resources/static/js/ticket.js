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
                const qrCont = document.querySelector('.qr-container');
                if(qrCont) qrCont.style.opacity = '0.2';
            }
        }, 1000);
    }

    // (6) Logic hiển thị Trạng Thái Phê Duyệt từ Admin
    const ticketStatus = localStorage.getItem('hutech_current_ticket_status') || 'success';
    const statusBadge = document.getElementById('ticket-status-badge');
    const qrContainer = document.querySelector('.qr-container');
    const qrHint = document.querySelector('.qr-hint');

    if (ticketStatus === 'pending') {
        statusBadge.textContent = 'Đang chờ Admin duyệt';
        statusBadge.style.backgroundColor = '#f59e0b'; // orange
        statusBadge.style.color = '#fff';
        qrContainer.style.display = 'none';
        qrHint.textContent = 'Mã QR sẽ xuất hiện sau khi Admin xác nhận thanh toán thành công.';
        qrHint.style.color = '#f59e0b';
    } else if (ticketStatus === 'failed') {
        statusBadge.textContent = 'Bị Từ Chối';
        statusBadge.style.backgroundColor = '#ef4444'; // red
        statusBadge.style.color = '#fff';
        qrContainer.style.display = 'none';
        qrHint.textContent = 'Thanh toán chưa thành công. Vui lòng liên hệ Admin.';
        qrHint.style.color = '#ef4444';
    } else {
        statusBadge.textContent = 'Đã Thanh Toán';
        statusBadge.style.backgroundColor = '#10b981'; // green
        statusBadge.style.color = '#fff';
        qrContainer.style.display = 'flex';
        qrHint.textContent = 'Vui lòng đưa mã này cho tài xế hoặc quét tại máy check-in cửa xe.';
        qrHint.style.color = '';
    }
});
