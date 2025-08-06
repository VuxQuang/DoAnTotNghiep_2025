// === Biến global ===
let tourId = null;
let adultPrice = 0;
let childPrice = 0;

// === Khi trang load ===
document.addEventListener('DOMContentLoaded', function () {
    initializeBookingForm();
    calculateTotal();
});

// === Khởi tạo form ===
function initializeBookingForm() {
    // Lấy tour ID từ hidden field
    const tourIdField = document.querySelector('input[name="tourId"]');
    if (tourIdField) {
        tourId = tourIdField.value;
    }

    // Lấy giá từ tour info
    const priceElement = document.querySelector('.price-value');
    if (priceElement) {
        const priceText = priceElement.textContent.replace(/[^\d]/g, '');
        adultPrice = parseInt(priceText);
        childPrice = adultPrice * 0.8; // Giả sử trẻ em = 80% giá người lớn
    }

    // Thêm event listeners
    addEventListeners();
}

// === Thêm event listeners ===
function addEventListeners() {
    // Event cho số lượng khách
    const countInputs = ['adultCount', 'childCount', 'infantCount'];
    countInputs.forEach(id => {
        const input = document.getElementById(id);
        if (input) {
            input.addEventListener('change', calculateTotal);
            input.addEventListener('input', calculateTotal);
        }
    });

    // Event cho schedule selection
    const scheduleInputs = document.querySelectorAll('input[name="scheduleId"]');
    scheduleInputs.forEach(input => {
        input.addEventListener('change', function() {
            updateScheduleInfo();
            calculateTotal();
        });
    });

    // Event cho form submission
    const form = document.querySelector('.booking-form');
    if (form) {
        form.addEventListener('submit', validateForm);
    }
}

// === Thay đổi số lượng ===
function changeCount(fieldId, change) {
    const input = document.getElementById(fieldId);
    if (!input) return;

    const currentValue = parseInt(input.value) || 0;
    const min = parseInt(input.min) || 0;
    const max = parseInt(input.max) || 999;

    let newValue = currentValue + change;
    
    // Đảm bảo giá trị trong khoảng min-max
    if (newValue < min) newValue = min;
    if (newValue > max) newValue = max;

    input.value = newValue;
    calculateTotal();
}

// === Tính tổng tiền ===
function calculateTotal() {
    const adultCount = parseInt(document.getElementById('adultCount').value) || 0;
    const childCount = parseInt(document.getElementById('childCount').value) || 0;
    const infantCount = parseInt(document.getElementById('infantCount').value) || 0;

    // Tính tiền từng loại
    const adultTotal = adultCount * adultPrice;
    const childTotal = childCount * childPrice;
    const infantTotal = 0; // Trẻ sơ sinh miễn phí

    const totalAmount = adultTotal + childTotal + infantTotal;
    const depositAmount = Math.round(totalAmount * 0.5); // Đặt cọc 50%
    const remainingAmount = totalAmount - depositAmount;

    // Cập nhật hiển thị
    updatePriceDisplay(adultTotal, childTotal, infantTotal, totalAmount, depositAmount, remainingAmount);

    // Gọi API để tính toán chính xác (nếu cần)
    if (tourId) {
        callCalculateTotalAPI(adultCount, childCount, infantCount);
    }
}

// === Cập nhật hiển thị giá ===
function updatePriceDisplay(adultTotal, childTotal, infantTotal, totalAmount, depositAmount, remainingAmount) {
    // Format số tiền
    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('vi-VN').format(amount) + '₫';
    };

    // Cập nhật từng phần
    const adultPriceElement = document.getElementById('adultPrice');
    const childPriceElement = document.getElementById('childPrice');
    const infantPriceElement = document.getElementById('infantPrice');
    const totalAmountElement = document.getElementById('totalAmount');
    const depositAmountElement = document.getElementById('depositAmount');
    const remainingAmountElement = document.getElementById('remainingAmount');

    if (adultPriceElement) adultPriceElement.textContent = formatCurrency(adultTotal);
    if (childPriceElement) childPriceElement.textContent = formatCurrency(childTotal);
    if (infantPriceElement) infantPriceElement.textContent = 'Miễn phí';
    if (totalAmountElement) totalAmountElement.textContent = formatCurrency(totalAmount);
    if (depositAmountElement) depositAmountElement.textContent = formatCurrency(depositAmount);
    if (remainingAmountElement) remainingAmountElement.textContent = formatCurrency(remainingAmount);
}

// === Gọi API tính tổng tiền ===
function callCalculateTotalAPI(adultCount, childCount, infantCount) {
    const formData = new FormData();
    formData.append('tourId', tourId);
    formData.append('adultCount', adultCount);
    formData.append('childCount', childCount);
    formData.append('infantCount', infantCount);

    fetch('/page/booking/calculate-total', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.error) {
            console.error('Lỗi tính toán:', data.error);
        } else {
            // Cập nhật với dữ liệu từ server
            updatePriceDisplayFromAPI(data);
        }
    })
    .catch(error => {
        console.error('Lỗi API:', error);
    });
}

// === Cập nhật giá từ API ===
function updatePriceDisplayFromAPI(data) {
    const totalAmountElement = document.getElementById('totalAmount');
    const depositAmountElement = document.getElementById('depositAmount');
    const remainingAmountElement = document.getElementById('remainingAmount');

    if (totalAmountElement) totalAmountElement.textContent = data.totalAmount + '₫';
    if (depositAmountElement) depositAmountElement.textContent = data.depositAmount + '₫';
    if (remainingAmountElement) remainingAmountElement.textContent = data.remainingAmount + '₫';
}

// === Cập nhật thông tin schedule ===
function updateScheduleInfo() {
    const selectedSchedule = document.querySelector('input[name="scheduleId"]:checked');
    if (!selectedSchedule) return;

    const scheduleId = selectedSchedule.value;
    const totalGuests = getTotalGuests();

    // Kiểm tra số chỗ còn lại
    checkAvailability(scheduleId, totalGuests);
}

// === Lấy tổng số khách ===
function getTotalGuests() {
    const adultCount = parseInt(document.getElementById('adultCount').value) || 0;
    const childCount = parseInt(document.getElementById('childCount').value) || 0;
    const infantCount = parseInt(document.getElementById('infantCount').value) || 0;
    
    return adultCount + childCount + infantCount;
}

// === Kiểm tra số chỗ còn lại ===
function checkAvailability(scheduleId, totalGuests) {
    const formData = new FormData();
    formData.append('scheduleId', scheduleId);
    formData.append('totalGuests', totalGuests);

    fetch('/page/booking/check-availability', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.error) {
            showError('Lỗi kiểm tra số chỗ: ' + data.error);
        } else {
            if (!data.available) {
                showError('Không đủ chỗ cho số lượng khách hàng này. Vui lòng chọn lịch khác hoặc giảm số lượng.');
            }
        }
    })
    .catch(error => {
        console.error('Lỗi API:', error);
    });
}

// === Validate form ===
function validateForm(event) {
    const errors = [];

    // Kiểm tra schedule
    const selectedSchedule = document.querySelector('input[name="scheduleId"]:checked');
    if (!selectedSchedule) {
        errors.push('Vui lòng chọn lịch khởi hành');
    }

    // Kiểm tra số lượng khách
    const adultCount = parseInt(document.getElementById('adultCount').value) || 0;
    if (adultCount < 1) {
        errors.push('Phải có ít nhất 1 người lớn');
    }

    // Kiểm tra thông tin liên hệ
    const customerName = document.getElementById('customerName').value.trim();
    const customerEmail = document.getElementById('customerEmail').value.trim();
    const customerPhone = document.getElementById('customerPhone').value.trim();

    if (!customerName) {
        errors.push('Vui lòng nhập họ và tên');
    }

    if (!customerEmail) {
        errors.push('Vui lòng nhập email');
    } else if (!isValidEmail(customerEmail)) {
        errors.push('Email không hợp lệ');
    }

    if (!customerPhone) {
        errors.push('Vui lòng nhập số điện thoại');
    } else if (!isValidPhone(customerPhone)) {
        errors.push('Số điện thoại không hợp lệ');
    }

    // Kiểm tra phương thức thanh toán
    const paymentMethod = document.querySelector('input[name="paymentMethod"]:checked');
    if (!paymentMethod) {
        errors.push('Vui lòng chọn phương thức thanh toán');
    }

    // Hiển thị lỗi nếu có
    if (errors.length > 0) {
        event.preventDefault();
        showErrors(errors);
        return false;
    }

    // Hiển thị loading
    showLoading();
    return true;
}

// === Validate email ===
function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

// === Validate phone ===
function isValidPhone(phone) {
    const phoneRegex = /^[0-9]{10,11}$/;
    return phoneRegex.test(phone);
}

// === Hiển thị lỗi ===
function showErrors(errors) {
    // Xóa alert cũ
    const oldAlert = document.querySelector('.alert');
    if (oldAlert) {
        oldAlert.remove();
    }

    // Tạo alert mới
    const alertDiv = document.createElement('div');
    alertDiv.className = 'alert alert-error';
    alertDiv.innerHTML = `
        <i class="fas fa-exclamation-circle"></i>
        <div>
            ${errors.map(error => `<div>${error}</div>`).join('')}
        </div>
    `;

    // Thêm vào đầu form
    const form = document.querySelector('.booking-form');
    form.insertBefore(alertDiv, form.firstChild);

    // Scroll to top
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

// === Hiển thị lỗi đơn lẻ ===
function showError(message) {
    showErrors([message]);
}

// === Hiển thị loading ===
function showLoading() {
    const submitBtn = document.querySelector('.btn-primary');
    if (submitBtn) {
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xử lý...';
        submitBtn.disabled = true;
    }
}

// === Reset loading ===
function resetLoading() {
    const submitBtn = document.querySelector('.btn-primary');
    if (submitBtn) {
        submitBtn.innerHTML = '<i class="fas fa-check"></i> Xác nhận đặt tour';
        submitBtn.disabled = false;
    }
}

// === Format số tiền ===
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

// === Smooth scroll to element ===
function scrollToElement(elementId) {
    const element = document.getElementById(elementId);
    if (element) {
        element.scrollIntoView({ 
            behavior: 'smooth', 
            block: 'center' 
        });
    }
}

// === Show notification ===
function showNotification(message, type = 'success') {
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.innerHTML = `
        <i class="fas fa-${type === 'success' ? 'check-circle' : 'exclamation-circle'}"></i>
        <span>${message}</span>
    `;

    document.body.appendChild(notification);

    // Hiển thị
    setTimeout(() => {
        notification.classList.add('show');
    }, 100);

    // Ẩn sau 3 giây
    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => {
            notification.remove();
        }, 300);
    }, 3000);
} 