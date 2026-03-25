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
    myTickets.forEach(ticket => {
        const ticketCard = document.createElement('div');
        ticketCard.className = 'glass-card ticket-item';

        const isCompleted = ticket.status === 'COMPLETED';

        // Xác định trạng thái thanh toán và màu sắc
        const isPaid = ticket.paymentMethod === 'TRANSFER' || ticket.paymentMethod === 'VNPAY' || ticket.paymentMethod === 'PASS';
        const statusText = ticket.paymentMethod === 'PASS' ? 'Thẻ Vé (0đ)' : (isPaid ? 'Đã Thanh Toán' : 'Thanh Toán Tại Xe');
        const statusColor = isPaid ? '#10b981' : '#f97316';
        const statusBg = isPaid ? 'rgba(16, 185, 129, 0.1)' : 'rgba(249, 115, 22, 0.1)';

        // Xử lý ngày giờ đặt vé
        let dateObj;
        if (Array.isArray(ticket.bookingTime)) {
             dateObj = new Date(ticket.bookingTime[0], ticket.bookingTime[1] - 1, ticket.bookingTime[2], ticket.bookingTime[3] || 0, ticket.bookingTime[4] || 0);
        } else {
             dateObj = new Date(ticket.bookingTime);
        }
        const dateStr = dateObj.toLocaleDateString('vi-VN');
        const timeStr = dateObj.toLocaleTimeString('vi-VN', {hour: '2-digit', minute:'2-digit'});

        // Xử lý ngày giờ hoàn thành (nếu có)
        let completedStr = '';
        if (isCompleted && ticket.completedAt) {
            let cObj;
            if (Array.isArray(ticket.completedAt)) {
                cObj = new Date(ticket.completedAt[0], ticket.completedAt[1] - 1, ticket.completedAt[2], ticket.completedAt[3] || 0, ticket.completedAt[4] || 0);
            } else {
                cObj = new Date(ticket.completedAt);
            }
            completedStr = cObj.toLocaleTimeString('vi-VN', {hour: '2-digit', minute:'2-digit'}) + ' ' + cObj.toLocaleDateString('vi-VN');
        }

        const seatStr = Array.isArray(ticket.seats) ? ticket.seats.join(', ') : ticket.seats;

        // Banner hoàn thành (chỉ hiện khi COMPLETED)
        const completedBanner = isCompleted ? `
            <div style="background: linear-gradient(135deg, #10b981, #059669); color: white; padding: 10px 16px; border-radius: 10px; margin-bottom: 12px; display: flex; align-items: center; gap: 10px; font-weight: 600; font-size: 0.9rem;">
                <span style="font-size: 1.3rem;">✅</span>
                <div>
                    <div>Chuyến xe đã hoàn thành</div>
                    ${completedStr ? `<div style="font-size:0.78rem; opacity:0.85; font-weight:400;">Lúc ${completedStr}</div>` : ''}
                </div>
            </div>` : '';

        const bottomArea = isCompleted
            ? `<div style="margin-top: 15px; font-size: 0.82rem; color: #10b981; font-weight: 600;">🎉 +10 H-Point đã được cộng!</div>`
            : `<div style="margin-top: 20px; display: flex; justify-content: space-between; align-items: flex-end;">
                <span style="font-size: 0.85rem; color: var(--text-muted);">Chạm để xem QR</span>
                <strong style="color: var(--primary); font-size: 1.2rem;">${(ticket.totalAmount).toLocaleString('vi-VN')} đ</strong>
               </div>`;

        ticketCard.style.borderLeft = isCompleted ? '4px solid #10b981' : '';

        ticketCard.innerHTML = `
            <div class="ticket-item-header">
                <span class="ticket-id">#${ticket.id.substring(ticket.id.length - 6).toUpperCase()}</span>
                <span style="color: ${statusColor}; font-size: 0.85rem; padding: 3px 8px; background: ${statusBg}; border-radius: 12px;">${statusText}</span>
            </div>
            ${completedBanner}
            <div style="margin-bottom: 10px; color: var(--text-main);"><strong>Tuyến:</strong> ${ticket.routeName}</div>
            <div style="margin-bottom: 10px; color: var(--text-muted);"><strong>Điểm đón:</strong> ${ticket.pickupPoint || 'Chưa xác định'}</div>
            <div style="margin-bottom: 10px; color: var(--text-muted);"><strong>Thời gian:</strong> ${dateStr} - ${timeStr}</div>
            <div style="margin-bottom: 10px; color: var(--text-muted);"><strong>Ghế ngồi:</strong> <span style="color: #38bdf8;">${seatStr}</span></div>
            ${bottomArea}
            <div class="ticket-icon">🎟️</div>
        `;

        // Chỉ cho phép click xem QR khi vé chưa completed
        if (!isCompleted) {
            ticketCard.addEventListener('click', () => {
                const seatsArray = Array.isArray(ticket.seats) ? ticket.seats : [];
                localStorage.setItem('hutech_booked_seats', JSON.stringify(seatsArray));
                localStorage.setItem('hutech_current_pickup_point', ticket.pickupPoint || 'Chưa xác định');
                localStorage.setItem('hutech_current_payment_status', statusText);
                window.location.href = '/ticket';
            });
        } else {
            ticketCard.style.cursor = 'default';
        }

        container.appendChild(ticketCard);
    });
});

