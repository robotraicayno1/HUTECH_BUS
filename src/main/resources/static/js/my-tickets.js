// Chờ cho toàn bộ HTML DOM được parse xong
document.addEventListener('DOMContentLoaded', () => {
    // Chỉ định vùng chứa danh sách vé
    const container = document.getElementById('ticket-list-container');
    
    function loadMyTickets() {
        // (1) Gọi API lấy danh sách vé của User đang đăng nhập từ MongoDB
        fetch('/api/tickets/my')
            .then(res => {
                if (!res.ok) throw new Error("Chưa đăng nhập hoặc lỗi mạng");
                return res.json();
            })
            .then(myTickets => {
                container.innerHTML = ''; // Xóa thẻ cũ mỗi lần refresh
                
                // (2) Kiểm tra trạng thái rỗng - Render Empty State
                if (!myTickets || myTickets.length === 0) {
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
                myTickets.slice().reverse().forEach(ticket => {
                    const ticketCard = document.createElement('div');
                    ticketCard.className = 'glass-card ticket-item';
                    
                    let statusHtml = '';
                    if (ticket.status === 'pending') {
                        statusHtml = `<span style="color: #f59e0b; font-size: 0.85rem; padding: 3px 8px; background: rgba(245, 158, 11, 0.1); border-radius: 12px;">Đang chờ duyệt</span>`;
                    } else if (ticket.status === 'failed') {
                        statusHtml = `<span style="color: #ef4444; font-size: 0.85rem; padding: 3px 8px; background: rgba(239, 68, 68, 0.1); border-radius: 12px;">Từ chối</span>`;
                    } else {
                        statusHtml = `<span style="color: #10b981; font-size: 0.85rem; padding: 3px 8px; background: rgba(16, 185, 129, 0.1); border-radius: 12px;">Đã Thanh Toán</span>`;
                    }
                    
                    // Nhồi nội dung HTML vào thẻ vé bằng Template String
                    ticketCard.innerHTML = `
                        <div class="ticket-item-header">
                            <span class="ticket-id">#${ticket.id}</span>
                            ${statusHtml}
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
                        const seatsArray = ticket.seats.split(', ');
                        localStorage.setItem('hutech_booked_seats', JSON.stringify(seatsArray));
                        localStorage.setItem('hutech_current_ticket_status', ticket.status || 'success');
                        
                        // Chuyển hướng sang trang chi tiết vé QR
                        window.location.href = '/ticket';
                    });
                    
                    // Nhét vé vừa tạo vào vùng chứa trên màn hình
                    container.appendChild(ticketCard);
                });
            })
            .catch(err => {
                console.error('Lỗi tải vé từ DB:', err);
                if (container.innerHTML === '') {
                    container.innerHTML = '<p style="text-align:center; padding: 2rem;">Lỗi tải dữ liệu vé. Vui lòng đăng nhập lại.</p>';
                }
            });
    }

    // Khởi chạy ngay lần đầu
    loadMyTickets();

    // Tự động đồng bộ với Admin mỗi 2 giây
    setInterval(loadMyTickets, 2000);
});
