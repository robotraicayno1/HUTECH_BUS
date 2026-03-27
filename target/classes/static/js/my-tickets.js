// Chờ cho toàn bộ HTML DOM được parse xong
document.addEventListener('DOMContentLoaded', async () => {
    // Chỉ định vùng chứa danh sách vé
    const container = document.getElementById('ticket-list-container');
    
    // (1) Lấy danh sách vé từ Cơ sở dữ liệu thông qua API
    let myTickets = [];
    try {
        const response = await fetch('/api/tickets/me');
        if (response.ok) {
            myTickets = await response.json();
        }
    } catch (error) {
        console.error("Lỗi khi tải danh sách vé: ", error);
    }
    
    // (2) Kiểm tra trạng thái rỗng - Render Empty State
    if (myTickets.length === 0) {
        container.innerHTML = `
            <div class="empty-state glass-card">
                <h3>Bạn chưa trải nghiệm chuyến xe nào 🚌</h3>
                <p>Hãy trở lại màn hình đặt chỗ để sắm cho mình chiếc vé trải nghiệm nhé!</p>
                <button onclick="window.location.href='/booking'" class="btn-checkout" style="width: auto; padding: 10px 30px; margin-top: 20px;">Đặt Ngay</button>
            </div>
        `;
        return;
    }
    
    // (3) Duyệt qua mảng vé và render ra thẻ HTML
    // Vì API trả về danh sách đã sort theo thời gian mới nhất, ta chỉ cần lặp
    myTickets.forEach(ticket => {
        // Tạo thẻ cha (div) cho thẻ vé
        const ticketCard = document.createElement('div');
        ticketCard.className = 'glass-card ticket-item';
        
        // Xác định trạng thái thanh toán và màu sắc
        const isPaid = ticket.paymentMethod === 'TRANSFER' || ticket.paymentMethod === 'VNPAY' || ticket.paymentMethod === 'PASS';
        const statusText = ticket.paymentMethod === 'PASS' ? 'Thẻ Vé (0đ)' : (isPaid ? 'Đã Thanh Toán' : 'Thanh Toán Tại Xe');
        const statusColor = isPaid ? '#10b981' : '#f97316'; // Xanh vs Cam
        const statusBg = isPaid ? 'rgba(16, 185, 129, 0.1)' : 'rgba(249, 115, 22, 0.1)';

        // Xử lý ngày giờ từ chuỗi ISO / Array của Java
        let dateObj;
        if (Array.isArray(ticket.bookingTime)) {
             dateObj = new Date(ticket.bookingTime[0], ticket.bookingTime[1] - 1, ticket.bookingTime[2], ticket.bookingTime[3] || 0, ticket.bookingTime[4] || 0);
        } else {
             dateObj = new Date(ticket.bookingTime);
        }
        const dateStr = dateObj.toLocaleDateString('vi-VN');
        const timeStr = dateObj.toLocaleTimeString('vi-VN', {hour: '2-digit', minute:'2-digit'});
        
        const seatStr = Array.isArray(ticket.seats) ? ticket.seats.join(', ') : ticket.seats;

        // Nhồi nội dung HTML vào thẻ vé bằng Template String
        ticketCard.innerHTML = `
            <div class="ticket-item-header">
                <span class="ticket-id">#${ticket.id.substring(ticket.id.length - 6).toUpperCase()}</span>
                <span style="color: ${statusColor}; font-size: 0.85rem; padding: 3px 8px; background: ${statusBg}; border-radius: 12px;">${statusText}</span>
            </div>
            <div style="margin-bottom: 10px; color: var(--text-main);"><strong>Tuyến:</strong> ${ticket.routeName}</div>
            <div style="margin-bottom: 10px; color: var(--text-muted);"><strong>Điểm đón:</strong> ${ticket.pickupPoint || 'Chưa xác định'}</div>
            <div style="margin-bottom: 10px; color: var(--text-muted);"><strong>Thời gian:</strong> ${dateStr} - ${timeStr}</div>
            <div style="margin-bottom: 10px; color: var(--text-muted);"><strong>Ghế ngồi:</strong> <span style="color: #38bdf8;">${seatStr}</span></div>
            <div style="margin-top: 20px; display: flex; justify-content: space-between; align-items: flex-end;">
                <span style="font-size: 0.85rem; color: var(--text-muted);">Chạm để xem QR</span>
                <strong style="color: var(--primary); font-size: 1.2rem;">${(ticket.totalAmount).toLocaleString('vi-VN')} đ</strong>
            </div>
            <div class="ticket-icon">🎟️</div>
        `;
        
        // (4) Ràng buộc sự kiện Click cho phép mở lại mã QR của vé này
        ticketCard.addEventListener('click', () => {
            // Cập nhật lại danh sách "ghế đang xem" và "điểm đón" vào localStorage để truyền cho trang ticket.js
            const seatsArray = Array.isArray(ticket.seats) ? ticket.seats : [];
            localStorage.setItem('hutech_booked_seats', JSON.stringify(seatsArray));
            localStorage.setItem('hutech_current_pickup_point', ticket.pickupPoint || 'Chưa xác định');
            localStorage.setItem('hutech_current_payment_status', statusText);
            
            // Chuyển hướng sang trang chi tiết vé QR
            window.location.href = '/ticket';
        });
        
        // Nhét vé vừa tạo vào vùng chứa trên màn hình
        container.appendChild(ticketCard);
    });
});
