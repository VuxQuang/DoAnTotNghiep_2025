document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('createUserForm');
    if (!form) return;

    form.addEventListener('submit', function(e) {
        // Xóa lỗi cũ
        removeAllErrors();
        let valid = true;
        // Validate từng trường
        if (!validateUsername()) valid = false;
        if (!validatePassword()) valid = false;
        if (!validateFullname()) valid = false;
        if (!validateEmail()) valid = false;
        if (!validatePhone()) valid = false;
        if (!validateRole()) valid = false;
        if (!validateAddress()) valid = false;
        if (!valid) e.preventDefault();
    });

    // Validate realtime khi blur
    form.querySelectorAll('input, select, textarea').forEach(input => {
        input.addEventListener('blur', function() {
            removeError(this);
            switch (this.name) {
                case 'username': validateUsername(); break;
                case 'password': validatePassword(); break;
                case 'fullname': validateFullname(); break;
                case 'email': validateEmail(); break;
                case 'phonenumber': validatePhone(); break;
                case 'role': validateRole(); break;
                case 'address': validateAddress(); break;
            }
        });
    });
});

function showError(input, message) {
    removeError(input);
    const error = document.createElement('div');
    error.className = 'field-error';
    error.style.cssText = `
        color: #e74c3c;
        font-size: 0.9rem;
        padding: 0.5rem 0.75rem;
        background: #fdf2f2;
        border: 1px solid #fecaca;
        border-radius: 6px;
        display: flex;
        align-items: center;
        gap: 0.5rem;
        width: 100%;
        position: absolute;
        top: 100%;
        left: 0;
        right: 0;
        z-index: 10;
        margin-top: 0.5rem;
    `;
    error.innerHTML = `<i class="fas fa-exclamation-circle"></i>${message}`;
    const formGroup = input.closest('.form-group');
    formGroup.style.position = 'relative';
    formGroup.appendChild(error);
    input.classList.add('error');
}
function removeError(input) {
    const formGroup = input.closest('.form-group');
    const err = formGroup.querySelector('.field-error');
    if (err) err.remove();
    input.classList.remove('error');
}
function removeAllErrors() {
    document.querySelectorAll('.field-error').forEach(e => e.remove());
    document.querySelectorAll('.error').forEach(e => e.classList.remove('error'));
}
function validateUsername() {
    const input = document.getElementById('username');
    const value = input.value.trim();
    if (!value) {
        showError(input, 'Tên đăng nhập là bắt buộc');
        return false;
    }
    if (value.length < 3) {
        showError(input, 'Tên đăng nhập phải có ít nhất 3 ký tự');
        return false;
    }
    if (!/^[a-zA-Z0-9_]+$/.test(value)) {
        showError(input, 'Chỉ chữ, số, gạch dưới');
        return false;
    }
    return true;
}
function validatePassword() {
    const input = document.getElementById('password');
    const value = input.value.trim();
    if (!value) {
        showError(input, 'Mật khẩu là bắt buộc');
        return false;
    }
    if (value.length < 6) {
        showError(input, 'Mật khẩu ít nhất 6 ký tự');
        return false;
    }
    if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(value)) {
        showError(input, 'Phải có chữ hoa, chữ thường, số');
        return false;
    }
    return true;
}
function validateFullname() {
    const input = document.getElementById('fullname');
    const value = input.value.trim();
    if (!value) {
        showError(input, 'Họ và tên là bắt buộc');
        return false;
    }
    if (value.length < 2) {
        showError(input, 'Họ và tên ít nhất 2 ký tự');
        return false;
    }
    return true;
}
function validateEmail() {
    const input = document.getElementById('email');
    const value = input.value.trim();
    if (!value) {
        showError(input, 'Email là bắt buộc');
        return false;
    }
    if (!/^\S+@\S+\.\S+$/.test(value)) {
        showError(input, 'Email không hợp lệ');
        return false;
    }
    return true;
}
function validatePhone() {
    const input = document.getElementById('phonenumber');
    const value = input.value.trim();
    if (!value) {
        showError(input, 'Số điện thoại là bắt buộc');
        return false;
    }
    if (!/^[0-9+\-\s()]{10,15}$/.test(value)) {
        showError(input, 'Số điện thoại không hợp lệ');
        return false;
    }
    return true;
}
function validateRole() {
    const input = document.getElementById('role');
    const value = input.value.trim();
    if (!value) {
        showError(input, 'Vui lòng chọn vai trò');
        return false;
    }
    return true;
}
function validateAddress() {
    const input = document.getElementById('address');
    const value = input.value.trim();
    if (!value) {
        showError(input, 'Địa chỉ là bắt buộc');
        return false;
    }
    if (value.length < 10) {
        showError(input, 'Địa chỉ ít nhất 10 ký tự');
        return false;
    }
    return true;
}
