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
function initBusLayout() {
    const container = document.getElementById('seats-container');
    container.innerHTML = '';
    
    // Giả lập một số ghế đã có người đặt
    const fakeBookedSeats = ['1A', '1B', '4C', '4D', '8A'];

    const columns = ['A', 'B', 'aisle', 'C', 'D']; // Sơ đồ: 2 ghế - Lối đi - 2 ghế

    for (let row = 1; row <= totalRows; row++) {
        columns.forEach((col, index) => {
            // Hàng cuối (hàng 10) sẽ bít lối đi thành 1 ghế (ví dụ ghế E)
            if (row === 10 && col === 'aisle') {
                createSeatElement(container, `${row}E`, fakeBookedSeats);
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
            createSeatElement(container, seatId, fakeBookedSeats);
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

// Gán sự kiện chuyển hướng qua trang Vé (Ticket/QR) sau khi bấm Thanh Toán
// Gán sự kiện chuyển hướng qua trang Vé (Ticket/QR) sau khi bấm Thanh Toán
document.getElementById('btn-checkout').addEventListener('click', () => {
    // 1. Lưu tạm ghế vào localStorage để truyền sang màn hình vẽ mã QR
    localStorage.setItem('hutech_booked_seats', JSON.stringify(selectedSeats));
    
    // 2. Mô phỏng lưu vé mới vào Cơ sở dữ liệu (Ở đây mượn LocalStorage)
    // - Đọc dữ liệu vé ảo cũ ra
    let myTickets = JSON.parse(localStorage.getItem('hutech_my_tickets') || '[]');
    
    // - Tạo thông tin vé chuẩn bị chèn
    const newTicket = {
        id: 'HT' + Math.floor(Math.random() * 1000000), // Random Mã vé
        date: new Date().toLocaleDateString('vi-VN'),    // Ngày hiện hành
        time: '07:30 AM',                                // Giờ mẫu
        route: 'Khu A - Khu E',                          // Tuyến mẫu
        seats: selectedSeats.join(', '),                 // Chuyển mảng ghế thành chuỗi chữ
        total: (selectedSeats.length * seatPrice)        // Tính tiền
    };
    
    // - Đẩy bản ghi mới vào danh sách và lưu ngược lại
    myTickets.push(newTicket);
    localStorage.setItem('hutech_my_tickets', JSON.stringify(myTickets));

    // 3. Chuyển hướng tới trang chi tiết vé
    window.location.href = '/ticket'; 
});
