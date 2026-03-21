// Chờ cho toàn bộ HTML DOM được parse xong
document.addEventListener('DOMContentLoaded', () => {
    // Chỉ định vùng chứa danh sách vé
    const container = document.getElementById('ticket-list-container');
    
    // (1) Lấy mảng dữ liệu vé do trang đặt chỗ (booking.js) đã lưu vào LocalStorage
    // Dữ liệu này được lưu dạng JSON nên cần parse (giải mã)
    const myTicketsJSON = localStorage.getItem('hutech_my_tickets');
    let myTickets = [];
    
    if (myTicketsJSON) {
        myTickets = JSON.parse(myTicketsJSON);
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
    // Chúng ta đảo ngược mảng (reverse) để vé vừa đặt được đẩy lên trên cùng
    myTickets.reverse().forEach(ticket => {
        // Tạo thẻ cha (div) cho thẻ vé
        const ticketCard = document.createElement('div');
        ticketCard.className = 'glass-card ticket-item';
        
        // Nhồi nội dung HTML vào thẻ vé bằng Template String
        ticketCard.innerHTML = `
            <div class="ticket-item-header">
                <span class="ticket-id">#${ticket.id}</span>
                <span style="color: #10b981; font-size: 0.85rem; padding: 3px 8px; background: rgba(16, 185, 129, 0.1); border-radius: 12px;">Đã Thanh Toán</span>
            </div>
            <div style="margin-bottom: 10px; color: var(--text-main);"><strong>Tuyến:</strong> ${ticket.route}</div>
            <div style="margin-bottom: 10px; color: var(--text-muted);"><strong>Thời gian:</strong> ${ticket.date} - ${ticket.time}</div>
            <div style="margin-bottom: 10px; color: var(--text-muted);"><strong>Ghế ngồi:</strong> <span style="color: #38bdf8;">${ticket.seats}</span></div>
            <div style="margin-top: 20px; display: flex; justify-content: space-between; align-items: flex-end;">
                <span style="font-size: 0.85rem; color: var(--text-muted);">Chạm để xem QR</span>
                <strong style="color: var(--primary); font-size: 1.2rem;">${(ticket.total).toLocaleString('vi-VN')} đ</strong>
            </div>
            <div class="ticket-icon">🎟️</div>
        `;
        
        // (4) Ràng buộc sự kiện Click cho phép mở lại mã QR của vé này
        ticketCard.addEventListener('click', () => {
            // Cập nhật lại danh sách "ghế đang xem" (hutech_booked_seats) 
            // Điều này ép trang vé (ticket.js) tạo lại mã QR phù hợp với vé đang click
            const seatsArray = ticket.seats.split(', ');
            localStorage.setItem('hutech_booked_seats', JSON.stringify(seatsArray));
            
            // Chuyển hướng sang trang chi tiết vé QR
            window.location.href = '/ticket';
        });
        
        // Nhét vé vừa tạo vào vùng chứa trên màn hình
        container.appendChild(ticketCard);
    });
});
