// Booking Form JavaScript
document.addEventListener('DOMContentLoaded', function() {
    initializeBookingForm();
});

let participantCounter = 0;

function initializeBookingForm() {
    // Lấy các elements
    const adultCountInput = document.getElementById('adultCount');
    const childCountInput = document.getElementById('childCount');
    const participantsContainer = document.getElementById('participants-container');
    const addParticipantBtn = document.getElementById('addParticipantBtn');
    
    // Lấy giá từ server
    const adultPriceElement = document.getElementById('adultPrice');
    const childPriceElement = document.getElementById('childPrice');
    const totalPriceElement = document.getElementById('totalPrice');
    
    const adultPrice = parseFloat(adultPriceElement.textContent.replace(/[^\d]/g, '')) || 0;
    const childPrice = parseFloat(childPriceElement.textContent.replace(/[^\d]/g, '')) || 0;
    
    // Thêm participant đầu tiên (người đặt)
    addParticipant('Người đặt', true);
    
    // Event listeners
    if (adultCountInput) {
        adultCountInput.addEventListener('change', updateParticipants);
        adultCountInput.addEventListener('input', updateTotalPrice); // Cập nhật real-time
    }
    
    if (childCountInput) {
        childCountInput.addEventListener('change', updateParticipants);
        childCountInput.addEventListener('input', updateTotalPrice); // Cập nhật real-time
    }
    
    if (addParticipantBtn) {
        addParticipantBtn.addEventListener('click', () => addParticipant('Người tham gia', false));
    }
    
    // Tính tổng tiền ban đầu
    updateTotalPrice();
}

function updateParticipants() {
    const adultCount = parseInt(document.getElementById('adultCount').value) || 0;
    const childCount = parseInt(document.getElementById('childCount').value) || 0;
    const participantsContainer = document.getElementById('participants-container');
    
    // Xóa tất cả participants hiện tại (trừ người đặt)
    const existingParticipants = participantsContainer.querySelectorAll('.participant-card');
    existingParticipants.forEach((participant, index) => {
        if (index > 0) { // Giữ lại người đặt (index 0)
            participant.remove();
        }
    });
    
    // Reset bộ đếm để đảm bảo index liên tục: [0] là người đặt, các mục tiếp theo bắt đầu từ 1
    participantCounter = 1;
    
    // Thêm participants cho người lớn
    for (let i = 1; i < adultCount; i++) {
        addParticipant(`Người lớn ${i + 1}`, false, 'ADULT');
    }
    
    // Thêm participants cho trẻ em
    for (let i = 0; i < childCount; i++) {
        addParticipant(`Trẻ em ${i + 1}`, false, 'CHILD');
    }
    
    // Cập nhật tổng tiền
    updateTotalPrice();
}

function addParticipant(title, isMainCustomer = false, type = 'ADULT') {
    const participantsContainer = document.getElementById('participants-container');
    participantCounter++;
    
    const participantCard = document.createElement('div');
    participantCard.className = 'participant-card';
    participantCard.innerHTML = `
        <div class="participant-header">
            <div class="participant-title">${title}</div>
            ${!isMainCustomer ? '<button type="button" class="btn-remove-participant" onclick="removeParticipant(this)"><i class="fas fa-times"></i></button>' : ''}
        </div>
        <div class="form-row">
            <div class="form-group">
                <label>Họ tên *</label>
                <input type="text" name="participants[${participantCounter - 1}].fullName" required>
            </div>
            <div class="form-group">
                <label>Ngày sinh *</label>
                <input type="date" name="participants[${participantCounter - 1}].dateOfBirth" required>
            </div>
        </div>
        <div class="form-row">
            <div class="form-group">
                <label>Giới tính *</label>
                <select name="participants[${participantCounter - 1}].gender" required>
                    <option value="">Chọn giới tính</option>
                    <option value="MALE">Nam</option>
                    <option value="FEMALE">Nữ</option>
                </select>
            </div>
            <div class="form-group">
                <label>CMND/CCCD *</label>
                <input type="text" name="participants[${participantCounter - 1}].idCard" 
                       pattern="[0-9]{9,12}" title="CMND/CCCD phải có 9-12 số" required>
            </div>
        </div>
        <div class="form-group">
            <label>Số điện thoại</label>
            <input type="tel" name="participants[${participantCounter - 1}].phoneNumber" 
                   pattern="[0-9]{10,11}" title="Số điện thoại phải có 10-11 số">
        </div>
        <input type="hidden" name="participants[${participantCounter - 1}].type" value="${type}">
    `;
    
    participantsContainer.appendChild(participantCard);
    
    // Animation
    participantCard.style.opacity = '0';
    participantCard.style.transform = 'translateY(20px)';
    setTimeout(() => {
        participantCard.style.transition = 'all 0.3s ease';
        participantCard.style.opacity = '1';
        participantCard.style.transform = 'translateY(0)';
    }, 10);
}

function removeParticipant(button) {
    const participantCard = button.closest('.participant-card');
    participantCard.style.transition = 'all 0.3s ease';
    participantCard.style.opacity = '0';
    participantCard.style.transform = 'translateY(-20px)';
    
    setTimeout(() => {
        participantCard.remove();
        updateTotalPrice();
    }, 300);
}

function updateTotalPrice() {
    const adultCount = parseInt(document.getElementById('adultCount').value) || 0;
    const childCount = parseInt(document.getElementById('childCount').value) || 0;
    
    // Lấy giá từ elements (hidden elements)
    const adultPriceElement = document.getElementById('adultPrice');
    const childPriceElement = document.getElementById('childPrice');
    const totalPriceElement = document.getElementById('totalPrice');
    const discountElement = document.getElementById('discountAmountValue');
    
    // Lấy elements hiển thị số tiền thực tế
    const adultPriceDisplayElement = document.getElementById('adultPriceDisplay');
    const childPriceDisplayElement = document.getElementById('childPriceDisplay');
    
    const adultPrice = parseFloat(adultPriceElement.textContent.replace(/[^\d]/g, '')) || 0;
    const childPrice = parseFloat(childPriceElement.textContent.replace(/[^\d]/g, '')) || 0;
    
    // Tính số tiền thực tế cho từng loại
    const adultTotal = adultCount * adultPrice;
    const childTotal = childCount * childPrice;
    
    // Cập nhật hiển thị số tiền thực tế
    if (adultPriceDisplayElement) {
        const formattedAdultTotal = new Intl.NumberFormat('vi-VN').format(adultTotal);
        adultPriceDisplayElement.textContent = formattedAdultTotal + '₫';
    }
    
    if (childPriceDisplayElement) {
        const formattedChildTotal = new Intl.NumberFormat('vi-VN').format(childTotal);
        childPriceDisplayElement.textContent = formattedChildTotal + '₫';
    }
    
    // Tính tổng tiền - xử lý trường hợp childCount = 0
    const subtotal = adultTotal + childTotal;
    const discount = discountElement ? (parseFloat(discountElement.textContent.replace(/[^\d]/g, '')) || 0) : 0;
    const totalAmount = Math.max(subtotal - discount, 0);
    
    // Format và hiển thị tổng tiền
    const formattedTotal = new Intl.NumberFormat('vi-VN').format(totalAmount);
    totalPriceElement.textContent = formattedTotal + '₫';
    
    // Highlight tổng tiền
    totalPriceElement.style.color = '#27ae60';
    totalPriceElement.style.fontWeight = 'bold';
    setTimeout(() => {
        totalPriceElement.style.color = '';
        totalPriceElement.style.fontWeight = '';
    }, 1000);
}

// Real-time validation
document.addEventListener('input', function(e) {
    if (e.target.matches('input[pattern]')) {
        const pattern = e.target.pattern;
        const value = e.target.value;
        const regex = new RegExp(pattern);
        
        if (value && !regex.test(value)) {
            e.target.setCustomValidity('Giá trị không hợp lệ');
        } else {
            e.target.setCustomValidity('');
        }
    }
});

// Auto-fill customer info if user is logged in
function autoFillCustomerInfo() {
    const customerNameInput = document.getElementById('customerName');
    const customerEmailInput = document.getElementById('customerEmail');
    const customerPhoneInput = document.getElementById('customerPhone');
    
    // Lấy thông tin từ user session (nếu có)
    // Có thể implement bằng cách lấy từ server hoặc localStorage
    
    if (customerNameInput && customerEmailInput && customerPhoneInput) {
        // Auto-fill first participant with customer info
        const firstParticipantName = document.querySelector('input[name="participants[0].fullName"]');
        if (firstParticipantName) {
            firstParticipantName.value = customerNameInput.value;
        }
    }
}

// Initialize auto-fill
setTimeout(autoFillCustomerInfo, 500);

// Show success/error messages
function showMessage(message, type = 'success') {
    const messageDiv = document.createElement('div');
    messageDiv.className = `message message-${type}`;
    messageDiv.textContent = message;
    
    messageDiv.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 25px;
        border-radius: 10px;
        color: white;
        font-weight: 600;
        z-index: 1000;
        transform: translateX(100%);
        transition: transform 0.3s ease;
        background: ${type === 'success' ? '#27ae60' : '#e74c3c'};
    `;
    
    document.body.appendChild(messageDiv);
    
    setTimeout(() => {
        messageDiv.style.transform = 'translateX(0)';
    }, 100);
    
    setTimeout(() => {
        messageDiv.style.transform = 'translateX(100%)';
        setTimeout(() => {
            if (document.body.contains(messageDiv)) {
                document.body.removeChild(messageDiv);
            }
        }, 300);
    }, 3000);
}

// Check for flash messages
window.addEventListener('load', function() {
    // Kiểm tra URL parameters cho messages
    const urlParams = new URLSearchParams(window.location.search);
    const success = urlParams.get('success');
    const error = urlParams.get('error');
    
    if (success) {
        showMessage(decodeURIComponent(success), 'success');
    }
    
    if (error) {
        showMessage(decodeURIComponent(error), 'error');
    }
});