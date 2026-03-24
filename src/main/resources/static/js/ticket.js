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

    // Lấy thông tin vé mới nhất hoặc tương ứng từ API để lấy Username và TicketId
    fetch('/api/tickets/my')
        .then(r => r.json())
        .then(async (tickets) => {
            const targetId = localStorage.getItem('hutech_current_ticket_id');
            let matchedTicket;
            if (targetId) {
                matchedTicket = tickets.find(t => t.id === targetId);
            }
            if (!matchedTicket) {
                matchedTicket = tickets.find(t => t.seats === bookedSeats.join(', '));
            }
            if (!matchedTicket && tickets.length > 0) matchedTicket = tickets[tickets.length - 1];
            
            const username = matchedTicket ? matchedTicket.username : 'Unknown';
            const routeId = matchedTicket ? matchedTicket.route : 'Khu A - Khu E';
            const ticketId = matchedTicket ? matchedTicket.id : 'N/A';
            
            // Nếu đây là Thẻ Định Kỳ, ghi đè Date hiển thị thành Hạn Sử Dụng
            if (matchedTicket && matchedTicket.type === 'PASS') {
                document.getElementById('ticket-date').textContent = 'HSD: ' + matchedTicket.expiryDate;
                // Ẩn bộ đếm thời gian 30 phút vì thẻ định kỳ không hết hạn theo phút
                const countdownWidget = document.querySelector('.countdown-widget');
                if (countdownWidget) countdownWidget.style.display = 'none';
                window.isPassTicket = true;
            }
            
            let passStatus = 'NONE';
            try {
                const passRes = await fetch('/api/passes/my');
                const passData = await passRes.json();
                if (passData && passData.status) {
                    passStatus = passData.status;
                }
            } catch(e){}

            // Rút gọn Mã QR chỉ chứa thông tin thiết yếu để tránh lỗi Tràn bộ nhớ chuỗi (Overflow) của qrcode.js
            // Backend DriverApiController chỉ cần TICKET_ID để quét
            const qrDataPayload = `TICKET_ID:${ticketId}|TYPE:${matchedTicket ? matchedTicket.type : 'TICKET'}`;
            console.log("Dữ liệu ẩn trong QR:", qrDataPayload);

            // Render QR Code
            const qrElement = document.getElementById("qrcode");
            qrElement.innerHTML = '';
            new QRCode(qrElement, {
                text: qrDataPayload,
                width: 160,
                height: 160,
                colorDark : "#000000",
                colorLight : "#ffffff",
                correctLevel : QRCode.CorrectLevel.L
            });
        }).catch(err => {
            console.error("Lỗi tạo QR:", err);
            const qrElement = document.getElementById("qrcode");
            qrElement.innerHTML = '<p style="color:red; font-size:12px;">Lỗi tạo QR: <br/>' + err.toString() + 
                                  '<br/>Payload size: ' + (typeof qrDataPayload !== 'undefined' ? qrDataPayload.length : 'N/A') +
                                  '<br/>' + (typeof qrDataPayload !== 'undefined' ? qrDataPayload.substring(0, 100) + '...' : '') + '</p>';
        });

    // (5) Logic đếm ngược 30 phút cho trang vé (chỉ chạy cho vé thường)
    let timer = 30 * 60;
    const display = document.getElementById('countdown-timer');
    if (display) {
        const countdownInterval = setInterval(function () {
            if (window.isPassTicket) {
                clearInterval(countdownInterval);
                return;
            }
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
