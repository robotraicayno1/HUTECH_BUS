// --- 1. Cấu hình Sơ đồ Xe Buýt ---
// Giả lập xe 45 chỗ (10 hàng, mỗi hàng 4 ghế, riêng hàng cuối 5 ghế)
const totalRows = 10;
const seatPrice = 10000; // Giá tiền mỗi vé (VD: 10.000 VNĐ)

// --- 1.1 Khởi tạo dữ liệu từ Server ---
let currentRouteId = new URLSearchParams(window.location.search).get('routeId') || '1'; 
let activeTrip = null;
let selectedSeats = []; // ĐÃ KHÔI PHỤC: Biến lưu trữ ghế đang chọn

// Khởi tạo sơ đồ khi tải trang
document.addEventListener('DOMContentLoaded', () => {
    fetchActiveTrip(); 
    startCountdown(30 * 60); 
});

async function fetchActiveTrip() {
    try {
        const response = await fetch(`/api/bookings/active/${currentRouteId}`);
        if (response.ok) {
            activeTrip = await response.json();
            document.getElementById('route-name-display').textContent = activeTrip.routeName;
            initBusLayout(activeTrip);
        } else {
            // Nếu không có chuyến đang chạy, vẫn cho hiện sơ đồ trống để test hoặc thông báo
            console.warn('Không tìm thấy chuyến xe đang chạy.');
            initBusLayout(null);
        }
    } catch (error) {
        console.error('Lỗi khi tải thông tin chuyến xe:', error);
        initBusLayout(null);
    }
}

// Hàm vẽ sơ đồ ghế có lối đi (Aisle) để khớp với CSS
function initBusLayout(tripData) {
    const container = document.getElementById('seats-container');
    container.innerHTML = '';
    
    const bookedSeatsList = tripData ? tripData.lockedSeats : [];
    const totalSeats = tripData ? tripData.totalSeats : 45;
    
    // Layout 5 cột: [Ghế] [Ghế] [Lối đi] [Ghế] [Ghế]
    // Với 45 ghế, ta cần khoảng 11 hàng (11 * 4 = 44, hàng cuối 5 ghế)
    // Để đơn giản và khớp CSS, ta chạy vòng lặp và chèn 'aisle' ở cột 3
    let seatCounter = 1;
    while (seatCounter <= totalSeats) {
        for (let col = 1; col <= 5; col++) {
            if (seatCounter > totalSeats) break;

            if (col === 3) {
                // Chèn lối đi
                const aisle = document.createElement('div');
                aisle.className = 'aisle';
                container.appendChild(aisle);
            } else {
                createSeatElement(container, seatCounter, bookedSeatsList);
                seatCounter++;
            }
        }
    }
}

// Hàm tự động vẽ sơ đồ ghế ngồi dựa trên dữ liệu thật từ Server
function initBusLayout(tripData) {
    const container = document.getElementById('seats-container');
    container.innerHTML = '';
    
    // Lấy TẤT CẢ các ghế đã bị chiếm dụng (Tiền mặt, Chuyển khoản, Online, Check-in)
    let bookedSeatsList = [];
    if (tripData) {
        bookedSeatsList = [
            ...(tripData.lockedSeats || []),
            ...(tripData.transferPaidSeats || []),
            ...(tripData.onlinePaidSeats || []),
            ...(tripData.onlineUnpaidSeats || []),
            ...(tripData.checkedInSeats || [])
        ];
    }

    const totalSeats = tripData ? tripData.totalSeats : 45;

    for (let i = 1; i <= totalSeats; i++) {
        createSeatElement(container, i, bookedSeatsList);
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
// Gán sự kiện thanh toán - Gửi dữ liệu về Server để xác nhận đặt chỗ
document.getElementById('btn-checkout').addEventListener('click', async () => {
    const btn = document.getElementById('btn-checkout');
    btn.disabled = true;
    btn.textContent = 'Đang xử lý...';

    // Đọc phương thức thanh toán
    const paymentMethod = document.querySelector('input[name="paymentMethod"]:checked')?.value || "TRANSFER";

    const bookingData = {
        routeId: currentRouteId,
        seatNumbers: selectedSeats.map(id => parseInt(id)),
        paymentType: paymentMethod
    };

    try {
        const response = await fetch('/api/bookings/reserve', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(bookingData)
        });

        if (response.ok) {
            // Nếu là Tiền mặt, kết thúc luôn
            if (paymentMethod === 'CASH') {
                localStorage.setItem('hutech_booked_seats', JSON.stringify(selectedSeats));
                
                // Lưu vào danh sách vé của tôi
                const myTickets = JSON.parse(localStorage.getItem('hutech_my_tickets') || '[]');
                const newTicket = {
                    id: Math.floor(Math.random() * 1000000),
                    route: document.getElementById('route-name-display').textContent,
                    date: new Date().toLocaleDateString('vi-VN'),
                    time: "07:30 AM", // Giả sử hoặc lấy từ UI nếu có
                    seats: selectedSeats.sort().join(', '),
                    total: selectedSeats.length * seatPrice,
                    paymentMethod: 'CASH'
                };
                myTickets.push(newTicket);
                localStorage.setItem('hutech_my_tickets', JSON.stringify(myTickets));

                alert('✅ Đặt chỗ thành công! Vui lòng thanh toán tiền mặt khi lên xe.');
                window.location.href = '/my-tickets';
                return;
            }

            // Nếu là Chuyển khoản, gọi VNPAY
            const paymentResponse = await fetch('/api/payment/create-payment', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    amount: selectedSeats.length * seatPrice,
                    routeId: currentRouteId
                })
            });

            if (paymentResponse.ok) {
                const paymentData = await paymentResponse.json();
                if (paymentData.code === "00") {
                    localStorage.setItem('hutech_booked_seats', JSON.stringify(selectedSeats));
                    window.location.href = paymentData.data; 
                } else {
                    alert('Lỗi tạo link thanh toán: ' + paymentData.message);
                    btn.disabled = false;
                    btn.textContent = 'Tiến Hành Đặt Chỗ';
                }
            } else {
                alert('Không thể kết nối dịch vụ thanh toán.');
                btn.disabled = false;
                btn.textContent = 'Tiến Hành Đặt Chỗ';
            }
        } else {
            const errorMsg = await response.text();
            alert('Lỗi đặt chỗ: ' + errorMsg);
            btn.disabled = false;
            btn.textContent = 'Thanh Toán Bằng QR';
        }
    } catch (error) {
        console.error('Lỗi kết nối:', error);
        alert('Không thể kết nối tới máy chủ.');
        btn.disabled = false;
    }
});

// Kiểm tra trạng thái thanh toán khi quay về từ VNPAY
window.addEventListener('load', function() {
    const urlParams = new URLSearchParams(window.location.search);
    const status = urlParams.get('vnpay_status');
    if (status === 'success') {
        alert('🎉 Thanh toán thành công! Ghế của bạn đã được xác nhận.');
    } else if (status === 'error') {
        alert('❌ Thanh toán không thành công. Vui lòng thử lại.');
    }
});
