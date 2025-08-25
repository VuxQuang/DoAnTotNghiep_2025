// Tour Search Component JavaScript
document.addEventListener('DOMContentLoaded', function() {
    initializePriceSlider();
    initializeFormValidation();
    initializeAutoSubmit();
});

// Khởi tạo Price Range Slider
function initializePriceSlider() {
    const minPriceSlider = document.getElementById('minPriceSlider');
    const maxPriceSlider = document.getElementById('maxPriceSlider');
    const minPriceInput = document.getElementById('minPrice');
    const maxPriceInput = document.getElementById('maxPrice');
    const sliderFill = document.querySelector('.slider-fill');
    const minPriceLabel = document.querySelector('.min-price-label');
    const maxPriceLabel = document.querySelector('.max-price-label');

    if (!minPriceSlider || !maxPriceSlider) return;

    const minVal = parseInt(minPriceSlider.min);
    const maxVal = parseInt(minPriceSlider.max);

    // Cập nhật slider fill
    function updateSliderFill() {
        const minPercent = ((minPriceSlider.value - minVal) / (maxVal - minVal)) * 100;
        const maxPercent = ((maxPriceSlider.value - minVal) / (maxVal - minVal)) * 100;

        sliderFill.style.left = minPercent + '%';
        sliderFill.style.width = (maxPercent - minPercent) + '%';
    }

    // Cập nhật input fields
    function updateInputs() {
        const minValue = parseInt(minPriceSlider.value);
        const maxValue = parseInt(maxPriceSlider.value);

        minPriceInput.value = minValue;
        maxPriceInput.value = maxValue;

        minPriceLabel.textContent = formatCurrency(minValue);
        maxPriceLabel.textContent = formatCurrency(maxValue);

        updateSliderFill();
    }

    // Cập nhật sliders từ input fields
    function updateSliders() {
        const minValue = parseInt(minPriceInput.value) || 0;
        const maxValue = parseInt(maxPriceInput.value) || maxVal;

        minPriceSlider.value = Math.min(minValue, maxValue);
        maxPriceSlider.value = Math.max(minValue, maxValue);

        updateSliderFill();
    }

    // Event listeners cho sliders
    minPriceSlider.addEventListener('input', function() {
        if (parseInt(minPriceSlider.value) > parseInt(maxPriceSlider.value)) {
            maxPriceSlider.value = minPriceSlider.value;
        }
        updateInputs();
    });

    maxPriceSlider.addEventListener('input', function() {
        if (parseInt(maxPriceSlider.value) < parseInt(minPriceSlider.value)) {
            minPriceSlider.value = maxPriceSlider.value;
        }
        updateInputs();
    });

    // Event listeners cho input fields
    minPriceInput.addEventListener('input', updateSliders);
    maxPriceInput.addEventListener('input', updateSliders);

    // Khởi tạo ban đầu
    updateSliderFill();
}

// Format tiền tệ
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
        minimumFractionDigits: 0
    }).format(amount);
}

// Khởi tạo Form Validation
function initializeFormValidation() {
    const form = document.getElementById('tourSearchForm');
    if (!form) return;

    form.addEventListener('submit', function(e) {
        if (!validateForm()) {
            e.preventDefault();
            showValidationError('Vui lòng kiểm tra lại thông tin tìm kiếm');
        }
    });
}

// Validate form
function validateForm() {
    const minPrice = document.getElementById('minPrice');
    const maxPrice = document.getElementById('maxPrice');
    const departureDateFrom = document.getElementById('departureDateFrom');
    const departureDateTo = document.getElementById('departureDateTo');

    // Kiểm tra khoảng giá
    if (minPrice.value && maxPrice.value) {
        if (parseInt(minPrice.value) > parseInt(maxPrice.value)) {
            showFieldError(minPrice, 'Giá tối thiểu không thể lớn hơn giá tối đa');
            return false;
        }
    }

    // Kiểm tra khoảng ngày
    if (departureDateFrom.value && departureDateTo.value) {
        const fromDate = new Date(departureDateFrom.value);
        const toDate = new Date(departureDateTo.value);

        if (fromDate > toDate) {
            showFieldError(departureDateFrom, 'Ngày bắt đầu không thể sau ngày kết thúc');
            return false;
        }

        // Kiểm tra ngày không được trong quá khứ
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        if (fromDate < today) {
            showFieldError(departureDateFrom, 'Ngày khởi hành không thể trong quá khứ');
            return false;
        }
    }

    return true;
}

// Hiển thị lỗi validation
function showValidationError(message) {
    // Xóa thông báo lỗi cũ
    const existingError = document.querySelector('.validation-error');
    if (existingError) {
        existingError.remove();
    }

    // Tạo thông báo lỗi mới
    const errorDiv = document.createElement('div');
    errorDiv.className = 'validation-error';
    errorDiv.style.cssText = `
        background: #ff4757;
        color: white;
        padding: 15px;
        border-radius: 10px;
        margin: 15px 0;
        text-align: center;
        font-weight: 600;
        animation: shake 0.5s ease-in-out;
    `;
    errorDiv.textContent = message;

    // Thêm CSS animation
    const style = document.createElement('style');
    style.textContent = `
        @keyframes shake {
            0%, 100% { transform: translateX(0); }
            25% { transform: translateX(-5px); }
            75% { transform: translateX(5px); }
        }
    `;
    document.head.appendChild(style);

    // Chèn thông báo lỗi vào đầu form
    const form = document.getElementById('tourSearchForm');
    form.insertBefore(errorDiv, form.firstChild);

    // Tự động ẩn sau 5 giây
    setTimeout(() => {
        if (errorDiv.parentNode) {
            errorDiv.remove();
        }
    }, 5000);
}

// Hiển thị lỗi cho field cụ thể
function showFieldError(field, message) {
    // Xóa lỗi cũ
    const existingError = field.parentNode.querySelector('.field-error');
    if (existingError) {
        existingError.remove();
    }

    // Tạo thông báo lỗi mới
    const errorSpan = document.createElement('span');
    errorSpan.className = 'field-error';
    errorSpan.style.cssText = `
        color: #ff4757;
        font-size: 0.85rem;
        margin-top: 5px;
        display: block;
        animation: fadeIn 0.3s ease-in;
    `;
    errorSpan.textContent = message;

    // Thêm CSS animation
    const style = document.createElement('style');
    style.textContent = `
        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(-10px); }
            to { opacity: 1; transform: translateY(0); }
        }
    `;
    document.head.appendChild(style);

    // Chèn thông báo lỗi sau field
    field.parentNode.appendChild(errorSpan);

    // Highlight field lỗi
    field.style.borderColor = '#ff4757';
    field.style.boxShadow = '0 0 0 3px rgba(255, 71, 87, 0.2)';

    // Focus vào field lỗi
    field.focus();

    // Tự động ẩn sau 5 giây
    setTimeout(() => {
        if (errorSpan.parentNode) {
            errorSpan.remove();
        }
        field.style.borderColor = '';
        field.style.boxShadow = '';
    }, 5000);
}

// Khởi tạo Auto Submit (tùy chọn)
function initializeAutoSubmit() {
    const form = document.getElementById('tourSearchForm');
    if (!form) return;

    // Auto submit khi thay đổi select fields
    const selectFields = form.querySelectorAll('select');
    selectFields.forEach(select => {
        select.addEventListener('change', function() {
            // Có thể bật auto submit ở đây nếu muốn
            // form.submit();
        });
    });

    // Auto submit khi thay đổi date fields
    const dateFields = form.querySelectorAll('input[type="date"]');
    dateFields.forEach(dateField => {
        dateField.addEventListener('change', function() {
            // Có thể bật auto submit ở đây nếu muốn
            // form.submit();
        });
    });
}

// Reset form
function resetSearch() {
    const form = document.getElementById('tourSearchForm');
    if (!form) return;

    // Reset form
    form.reset();

    // Reset price slider
    const minPriceSlider = document.getElementById('minPriceSlider');
    const maxPriceSlider = document.getElementById('maxPriceSlider');
    if (minPriceSlider && maxPriceSlider) {
        minPriceSlider.value = 0;
        maxPriceSlider.value = 20000000;
        initializePriceSlider();
    }

    // Reset price input fields
    const minPriceInput = document.getElementById('minPrice');
    const maxPriceInput = document.getElementById('maxPrice');
    if (minPriceInput && maxPriceInput) {
        minPriceInput.value = '';
        maxPriceInput.value = '';
    }

    // Reset labels
    const minPriceLabel = document.querySelector('.min-price-label');
    const maxPriceLabel = document.querySelector('.max-price-label');
    if (minPriceLabel && maxPriceLabel) {
        minPriceLabel.textContent = '0 VNĐ';
        maxPriceLabel.textContent = '20,000,000 VNĐ';
    }

    // Xóa tất cả thông báo lỗi
    const errors = document.querySelectorAll('.validation-error, .field-error');
    errors.forEach(error => error.remove());

    // Reset border colors
    const inputs = form.querySelectorAll('input, select');
    inputs.forEach(input => {
        input.style.borderColor = '';
        input.style.boxShadow = '';
    });

    // Hiển thị thông báo thành công
    showSuccessMessage('Đã đặt lại tất cả bộ lọc');
}

// Hiển thị thông báo thành công
function showSuccessMessage(message) {
    // Xóa thông báo cũ
    const existingMessage = document.querySelector('.success-message');
    if (existingMessage) {
        existingMessage.remove();
    }

    // Tạo thông báo thành công mới
    const successDiv = document.createElement('div');
    successDiv.className = 'success-message';
    successDiv.style.cssText = `
        background: #2ed573;
        color: white;
        padding: 15px;
        border-radius: 10px;
        margin: 15px 0;
        text-align: center;
        font-weight: 600;
        animation: slideInDown 0.5s ease-out;
    `;
    successDiv.textContent = message;

    // Thêm CSS animation
    const style = document.createElement('style');
    style.textContent = `
        @keyframes slideInDown {
            from { opacity: 0; transform: translateY(-20px); }
            to { opacity: 1; transform: translateY(0); }
        }
    `;
    document.head.appendChild(style);

    // Chèn thông báo thành công vào đầu form
    const form = document.getElementById('tourSearchForm');
    form.insertBefore(successDiv, form.firstChild);

    // Tự động ẩn sau 3 giây
    setTimeout(() => {
        if (successDiv.parentNode) {
            successDiv.remove();
        }
    }, 3000);
}

// Reset function cho Mini version
function resetSearchMini() {
    const form = document.getElementById('tourSearchFormMini');
    if (!form) return;

    // Reset form
    form.reset();

    // Xóa tất cả thông báo lỗi
    const errors = document.querySelectorAll('.validation-error, .field-error');
    errors.forEach(error => error.remove());

    // Reset border colors
    const inputs = form.querySelectorAll('input, select');
    inputs.forEach(input => {
        input.style.borderColor = '';
        input.style.boxShadow = '';
    });

    // Hiển thị thông báo thành công
    showSuccessMessage('Đã làm mới tìm kiếm');
}

// Apply quick filter
function applyQuickFilter(filterType) {
    const form = document.getElementById('tourSearchForm');
    if (!form) return;

    // Clear previous filters
    form.reset();

    // Apply specific filter
    switch(filterType) {
        case 'hot':
            // Set rating to 4.5+
            const ratingSelect = document.getElementById('sortBy');
            if (ratingSelect) ratingSelect.value = 'rating';
            showSuccessMessage('Đã áp dụng bộ lọc Tour Hot');
            break;

        case 'discount':
            // Set price range to show all (you can customize this)
            const minPriceInput = document.getElementById('minPrice');
            const maxPriceInput = document.getElementById('maxPrice');
            if (minPriceInput && maxPriceInput) {
                minPriceInput.value = '';
                maxPriceInput.value = '';
            }
            showSuccessMessage('Đã áp dụng bộ lọc Tour Giảm Giá');
            break;

        case 'new':
            // Set sort to newest
            const sortSelect = document.getElementById('sortBy');
            if (sortSelect) sortSelect.value = 'createdAt';
            showSuccessMessage('Đã áp dụng bộ lọc Tour Mới');
            break;
    }

    // Auto submit form
    setTimeout(() => {
        form.submit();
    }, 1000);
}

// Export functions để sử dụng từ bên ngoài
window.TourSearch = {
    resetSearch,
    resetSearchMini,
    validateForm,
    showValidationError,
    showFieldError,
    showSuccessMessage,
    applyQuickFilter
};
