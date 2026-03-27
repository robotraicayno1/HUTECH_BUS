// --- 1. Cấu hình Sơ đồ Xe Buýt ---
// Giả lập xe 45 chỗ (10 hàng, mỗi hàng 4 ghế, riêng hàng cuối 5 ghế)
const totalRows = 10;
const seatPrice = 10000; // Giá tiền mỗi vé (VD: 10.000 VNĐ)

// --- 1.1 Khởi tạo dữ liệu từ Server ---
let currentRouteId = new URLSearchParams(window.location.search).get('routeId') || '1'; 
let currentTripId = new URLSearchParams(window.location.search).get('tripId');
let activeTrip = null;
let selectedSeats = []; 
let userPass = null; // Lưu trữ thông tin thẻ vé của user
let availableVouchers = [];

// Khởi tạo sơ đồ khi tải trang
document.addEventListener('DOMContentLoaded', async () => {
    await checkUserPass(); 
    await fetchActiveTrip(); 
    fetchRouteStops(); 
    fetchAvailableVouchers(); // Lấy danh sách voucher
    startCountdown(30 * 60); 

    document.getElementById('voucher-select').addEventListener('change', updateSummary);
});

async function checkUserPass() {
    try {
        const response = await fetch('/api/users/me/active-pass');
        if (response.ok) {
            const data = await response.json();
            if (data.hasActivePass) {
                userPass = data.pass;
                console.log('User has active pass:', userPass);
            }
        }
    } catch (error) {
        console.error('Lỗi khi kiểm tra thẻ vé:', error);
    }
}

async function fetchActiveTrip() {
    try {
        const url = currentTripId 
            ? `/api/bookings/active-trip/${currentTripId}`
            : `/api/bookings/active/${currentRouteId}`;
            
        const response = await fetch(url);
        if (response.ok) {
            activeTrip = await response.json();
            document.getElementById('route-name-display').textContent = activeTrip.routeName;
            if (activeTrip.vehicleLicensePlate) {
                document.getElementById('vehicle-plate-display').textContent = activeTrip.vehicleLicensePlate;
            }
            initBusLayout(activeTrip);
        } else {
            console.warn('Không tìm thấy chuyến xe đang chạy.');
            initBusLayout(null);
        }
    } catch (error) {
        console.error('Lỗi khi tải thông tin chuyến xe:', error);
        initBusLayout(null);
    }
}

async function fetchRouteStops() {
    try {
        const response = await fetch(`/api/routes/${currentRouteId}`);
        if (response.ok) {
            const data = await response.json();
            const stops = data.stops || [];
            
            const pickupSelect = document.getElementById('pickup-point-select');
            const dropoffSelect = document.getElementById('dropoff-point-select');
            
            const options = stops.map(s => `<option value="${s.name}">${s.name}</option>`).join('');
            pickupSelect.innerHTML = options;
            
            // Drop-off points are usually shown in reverse order for convenience, 
            // but we can just use the same list or reverse it.
            dropoffSelect.innerHTML = stops.map(s => `<option value="${s.name}">${s.name}</option>`).reverse().join('');
        }
    } catch (error) {
        console.error('Lỗi khi tải danh sách trạm đón:', error);
    }
}

async function fetchAvailableVouchers() {
    try {
        const response = await fetch('/api/bookings/vouchers');
        if (response.ok) {
            availableVouchers = await response.json();
            const select = document.getElementById('voucher-select');
            select.innerHTML = '<option value="">-- Không sử dụng --</option>';
            availableVouchers.forEach(v => {
                const option = document.createElement('option');
                option.value = v.id;
                option.textContent = `${v.voucherName} (-${v.discountAmount.toLocaleString()}đ)`;
                select.appendChild(option);
            });
        }
    } catch (error) {
        console.error('Lỗi khi tải danh sách voucher:', error);
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
    container.dataset.total = totalSeats;

    for (let i = 1; i <= totalSeats; i++) {
        createSeatElement(container, i, bookedSeatsList);
    }
}

// Xây dựng 1 phần tử Ghế trong DOM với đúng cột trong grid
function createSeatElement(container, seatId, bookedSeatsList) {
    const totalSeats = container.dataset.total ? parseInt(container.dataset.total) : 45;

    // Tính vị trí cột trong layout 5 cột: [A | B | aisle | C | D]
    // Xe 45 ghế: 40 ghế thường (10 hàng × 4) + hàng cuối 5 ghế
    // Ghế thường: theo thứ tự 1,2,3,4 → cột 1,2,4,5
    const COL_MAP = [1, 2, 4, 5]; // cột tương ứng cho ghế 1→4 trong mỗi hàng
    let gridCol = null;

    if (totalSeats >= 16) {
        const seatsPerRow = 4;
        if (seatId <= (Math.floor(totalSeats / seatsPerRow) * seatsPerRow)) {
            const posInRow = (seatId - 1) % seatsPerRow;
            gridCol = COL_MAP[posInRow];
        } else {
            // Special handling for the last row (e.g. 45-seat bus)
            if (totalSeats === 45 && seatId > 40) {
                gridCol = seatId - 40; // 1 → 5
            } else {
                // Just spread them (fallback)
                gridCol = (seatId % 5) || 5;
            }
        }
    }

    const seat = document.createElement('div');
    seat.className = 'seat';
    seat.textContent = seatId;
    seat.dataset.id = seatId;
    if (gridCol) seat.style.gridColumn = gridCol;

    if (bookedSeatsList.includes(parseInt(seatId))) {
        seat.classList.add('booked');
    } else {
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
        let total = selectedSeats.length * seatPrice;
        
        // Nếu có thẻ vé, đổi nút và giá
        if (userPass) {
            total = 0; // Miễn phí từng chuyến
            btnCheckout.textContent = 'Xác nhận bằng ' + (userPass.type === 'WEEK' ? 'Thẻ Tuần' : userPass.type === 'MONTH' ? 'Thẻ Tháng' : 'Thẻ Năm');
        } else {
            btnCheckout.textContent = 'Tiến Hành Đặt Chỗ';
        }

        // --- XỬ LÝ VOUCHER (Luôn hiển thị nếu có) ---
        const selectedVoucherId = document.getElementById('voucher-select').value;
        if (selectedVoucherId) {
            const voucher = availableVouchers.find(v => v.id === selectedVoucherId);
            if (voucher) {
                const discount = Math.min(total, voucher.discountAmount);
                total -= discount;
                
                // Update totalPriceEl to show original price struck through and discounted price
                totalPriceEl.innerHTML = `<span style="text-decoration: line-through; color: #999; font-size: 0.8rem; margin-right: 8px;">${(selectedSeats.length * seatPrice).toLocaleString()}đ</span>` + 
                                         `<span>${total.toLocaleString('vi-VN')} VNĐ</span>`;
                
                // If not using a pass, update button text with discounted price
                if (!userPass) {
                    btnCheckout.textContent = 'Tiến Hành Đặt Chỗ (' + total.toLocaleString('vi-VN') + ' VNĐ)';
                }
            }
        } else {
            // If no voucher selected, display total price normally
            totalPriceEl.textContent = total.toLocaleString('vi-VN') + ' VNĐ';
            if (!userPass) {
                btnCheckout.textContent = 'Tiến Hành Đặt Chỗ (' + total.toLocaleString('vi-VN') + ' VNĐ)';
            }
        }
        
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
    const pickupPoint = document.getElementById('pickup-point-select').value;
    const dropoffPoint = document.getElementById('dropoff-point-select').value;

    const bookingData = {
        routeId: currentRouteId,
        tripId: currentTripId || (activeTrip ? activeTrip.id : null),
        seatNumbers: selectedSeats.map(id => parseInt(id)),
        paymentType: userPass ? "PASS" : paymentMethod,
        pickupPoint: pickupPoint,
        dropoffPoint: dropoffPoint,
        voucherId: document.getElementById('voucher-select').value
    };

    try {
        const response = await fetch('/api/bookings/reserve', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(bookingData)
        });

        if (response.ok) {
            // Nếu dùng thẻ vé hoặc Tiền mặt, kết thúc luôn
            if (userPass || paymentMethod === 'CASH') {
                localStorage.setItem('hutech_booked_seats', JSON.stringify(selectedSeats));
                localStorage.setItem('hutech_current_pickup_point', pickupPoint);
                localStorage.setItem('hutech_current_payment_status', userPass ? 'Sử dụng Thẻ Vé' : 'Thanh Toán Tại Xe');

                alert(userPass ? '✅ Xác nhận ghế thành công bằng Thẻ Vé!' : '✅ Đặt chỗ thành công! Vui lòng thanh toán tiền mặt khi lên xe.');
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
                    localStorage.setItem('hutech_current_pickup_point', pickupPoint);
                    localStorage.setItem('hutech_current_dropoff_point', dropoffPoint);
                    localStorage.setItem('hutech_current_payment_status', 'Đã Thanh Toán');
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
