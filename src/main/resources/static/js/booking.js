// --- 1. Cấu hình Sơ đồ Xe Buýt ---
// Giả lập xe 45 chỗ (10 hàng, mỗi hàng 4 ghế, riêng hàng cuối 5 ghế)
const totalRows = 10;
const seatPrice = 10000; // Giá tiền mỗi vé (VD: 10.000 VNĐ)

// Lưu trữ các ghế đã chọn
let selectedSeats = [];

// Khởi tạo sơ đồ khi tải trang
document.addEventListener('DOMContentLoaded', () => {
    initBusLayout();
    startCountdown(30 * 60); // Đếm ngược 30 phút (tính bằng giây)
});

// Hàm tự động vẽ sơ đồ ghế ngồi
async function initBusLayout() {
    const container = document.getElementById('seats-container');
    container.innerHTML = '';

    // Lấy ghế đã đặt từ backend
    const route = 'Khu A - Khu E';
    const date = new Date().toLocaleDateString('vi-VN');
    const time = '07:30 AM';
    let bookedSeatsList = [];
    try {
        const res = await fetch(`/api/tickets/booked-seats?route=${encodeURIComponent(route)}&date=${encodeURIComponent(date)}&time=${encodeURIComponent(time)}`);
        bookedSeatsList = await res.json();
    } catch (e) {
        console.error(e);
    }

    const columns = ['A', 'B', 'aisle', 'C', 'D']; // Sơ đồ: 2 ghế - Lối đi - 2 ghế

    for (let row = 1; row <= totalRows; row++) {
        columns.forEach((col, index) => {
            // Hàng cuối (hàng 10) sẽ bít lối đi thành 1 ghế (ví dụ ghế E)
            if (row === 10 && col === 'aisle') {
                createSeatElement(container, `${row}E`, bookedSeatsList);
                return;
            }

            // Với các hàng khác, nếu là 'aisle' thì render 1 khoảng trống
            if (col === 'aisle') {
                const aisle = document.createElement('div');
                aisle.className = 'aisle';
                container.appendChild(aisle);
                return;
            }

            // Render ghế bình thường
            const seatId = `${row}${col}`;
            createSeatElement(container, seatId, bookedSeatsList);
        });
    }
}

// Xây dựng 1 phần tử Ghế trong DOM
function createSeatElement(container, seatId, bookedSeatsList) {
    const seat = document.createElement('div');
    seat.className = 'seat';
    seat.textContent = seatId;
    seat.dataset.id = seatId; // Gắn data-id để dễ truy xuất

    // Kiểm tra nếu ghế nằm trong mảng đã bị người khác đặt
    if (bookedSeatsList.includes(seatId)) {
        seat.classList.add('booked');
    } else {
        // Nếu ghế trống, gán sự kiện click để chọn/bỏ chọn
        seat.addEventListener('click', handleSeatClick);
    }

    container.appendChild(seat);
}

// Hàm xử lý khi user click vào 1 ghế trống
function handleSeatClick(e) {
    const seat = e.target;
    const seatId = seat.dataset.id;

    if (seat.classList.contains('selected')) {
        // Hủy chọn
        seat.classList.remove('selected');
        selectedSeats = selectedSeats.filter(id => id !== seatId);
    } else {
        // Chọn mới (giới hạn tối đa 5 vé mỗi lần mua để chống spam)
        if (selectedSeats.length >= 5) {
            alert('Bạn chỉ được chọn tối đa 5 ghế trong một lần đặt!');
            return;
        }
        seat.classList.add('selected');
        selectedSeats.push(seatId);
    }

    updateSummary();
}

// Hàm cập nhật khu vực hiển thị Tóm tắt và tính tiền
function updateSummary() {
    const selectedListEl = document.getElementById('selected-seats-list');
    const totalPriceEl = document.getElementById('total-price');
    const btnCheckout = document.getElementById('btn-checkout');

    if (selectedSeats.length === 0) {
        selectedListEl.textContent = 'Chưa chọn';
        totalPriceEl.textContent = '0 VNĐ';
        btnCheckout.disabled = true;
    } else {
        // Hiển thị danh sách ghế sắp xếp theo bảng chữ cái/số
        selectedSeats.sort();
        selectedListEl.textContent = selectedSeats.join(', ');

        // Cập nhật giá tiền
        const total = selectedSeats.length * seatPrice;
        totalPriceEl.textContent = total.toLocaleString('vi-VN') + ' VNĐ';

        // Kích hoạt nút thanh toán
        btnCheckout.disabled = false;
    }
}

// --- 2. Xử lý logic hiển thị đếm ngược 30 phút ---
// Mục đích: Ép user phải chốt giao dịch trong thời gian quy định
let countdownInterval;

function startCountdown(durationInSeconds) {
    let timer = durationInSeconds;
    const display = document.getElementById('countdown-timer');

    // Chạy vòng lặp real-time mỗi giây
    countdownInterval = setInterval(function () {
        // Tính toán số phút và số giây còn lại
        let minutes = parseInt(timer / 60, 10);
        let seconds = parseInt(timer % 60, 10);

        // Chuẩn hóa chuỗi (thêm số 0 phía trước nếu chữ số < 10)
        minutes = minutes < 10 ? "0" + minutes : minutes;
        seconds = seconds < 10 ? "0" + seconds : seconds;

        // Cập nhật UI
        display.textContent = minutes + ":" + seconds;

        // Nếu hết giờ
        if (--timer < 0) {
            clearInterval(countdownInterval);
            display.textContent = "00:00";
            alert('Thời gian giữ chỗ đã hết! Vui lòng refresh và đặt lại.');
            // Vô hiệu hóa toàn bộ tương tác
            document.querySelectorAll('.seat').forEach(s => s.style.pointerEvents = 'none');
            document.getElementById('btn-checkout').disabled = true;
        }
    }, 1000);
}

// --- 3. Xử lý Thanh toán qua VietQR ---
// Cấu hình ngân hàng VietQR (Bạn có thể thay đổi thông tin này)
const BANK_ID = "MB"; // MB Bank
const ACCOUNT_NO = "0795866200"; // Số tài khoản
const ACCOUNT_NAME = "VO HOANG GIANG KHANG"; // Tên chủ tài khoản

const paymentModal = document.getElementById('payment-modal');
const btnFinishPayment = document.getElementById('btn-finish-payment');
const vietqrImg = document.getElementById('vietqr-img');

// Mở modal thanh toán khi bấm "Thanh Toán Bằng QR"
document.getElementById('btn-checkout').addEventListener('click', () => {
    const totalAmount = selectedSeats.length * seatPrice;
    const orderInfo = `Ve xe HUTECH ${selectedSeats.join('')}`;

    // Tạo link QR từ VietQR API
    const qrUrl = `https://img.vietqr.io/image/${BANK_ID}-${ACCOUNT_NO}-qr_only.png?amount=${totalAmount}&addInfo=${encodeURIComponent(orderInfo)}&accountName=${encodeURIComponent(ACCOUNT_NAME)}`;

    // Cập nhật text trong modal
    document.getElementById('modal-total-amount').textContent = totalAmount.toLocaleString('vi-VN') + ' VNĐ';
    document.getElementById('modal-transfer-content').textContent = orderInfo;

    vietqrImg.src = qrUrl;
    paymentModal.classList.remove('hidden');

    // Tự động đẩy vé sang hệ thống Admin ở trạng thái chờ duyệt (Pending) NGAY LẬP TỨC
    localStorage.setItem('hutech_booked_seats', JSON.stringify(selectedSeats));
    localStorage.setItem('hutech_current_ticket_status', 'pending');

    const newTicket = {
        id: 'HT' + Math.floor(Math.random() * 1000000),
        date: new Date().toLocaleDateString('vi-VN'),
        time: '07:30 AM',
        route: 'Khu A - Khu E',
        seats: selectedSeats.join(', '),
        total: (selectedSeats.length * seatPrice),
        status: 'pending' // Chờ hệ thống tự quét
    };

    fetch('/api/tickets', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(newTicket)
    }).then(() => {
        // LIÊN TỤC KIỂM TRA TRẠNG THÁI VÉ
        const checkInterval = setInterval(() => {
            fetch('/api/tickets/my')
            .then(r => r.json())
            .then(myTickets => {
                const checkTicket = myTickets.find(t => t.id === newTicket.id);
                // Đợi đến khi Admin duyệt (hoặc Backend tự duyệt sau 5s)
                if (checkTicket && (checkTicket.status === 'success' || checkTicket.status === 'confirmed')) {
                    clearInterval(checkInterval);
                    localStorage.setItem('hutech_current_ticket_status', 'success');
                    window.location.href = '/ticket';
                } else if (checkTicket && checkTicket.status === 'failed') {
                    clearInterval(checkInterval);
                    alert('Rất tiếc, ghế này vừa bị người khác mua trước! Vé đã bị hủy.');
                    window.location.reload();
                }
            })
            .catch(err => console.error('Lỗi khi kiểm tra vé:', err));
        }, 1500); // Mỗi 1.5 giây check CSDL 1 lần
    }).catch(err => {
        console.error('Lỗi khi lưu DB:', err);
    });
});

