document.addEventListener('DOMContentLoaded', function() {
    const form = document.querySelector('.login-form');
    if (!form) return;

    form.addEventListener('submit', function(e) {
        // Xóa lỗi cũ
        removeAllErrors();
        let valid = true;
        // Validate từng trường
        if (!validateEmail()) valid = false;
        if (!validatePhone()) valid = false;
        if (!validatePassword()) valid = false;
        if (!valid) e.preventDefault();
    });

    // Validate realtime khi blur và input
    form.querySelectorAll('input').forEach(input => {
        input.addEventListener('blur', function() {
            removeError(this);
            switch (this.name) {
                case 'email': validateEmail(); break;
                case 'phoneNumber': validatePhone(); break;
                case 'password': validatePassword(); break;
            }
        });
        
        // Real-time validation cho password
        if (input.name === 'password') {
            input.addEventListener('input', function() {
                removeError(this);
                validatePassword();
            });
        }
    });

    // Toggle password visibility
    const togglePassword = document.querySelector('.toggle-password');
    const passwordInput = document.querySelector('input[name="password"]');
    
    if (togglePassword && passwordInput) {
        togglePassword.addEventListener('click', function() {
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordInput.setAttribute('type', type);
            this.classList.toggle('fa-eye');
            this.classList.toggle('fa-eye-slash');
        });
    }
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

function showSuccess(input, message) {
    removeError(input);
    const success = document.createElement('div');
    success.className = 'field-success';
    success.style.cssText = `
        color: #10b981;
        font-size: 0.9rem;
        padding: 0.5rem 0.75rem;
        background: #f0fdf4;
        border: 1px solid #bbf7d0;
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
    success.innerHTML = `<i class="fas fa-check-circle"></i>${message}`;
    const formGroup = input.closest('.form-group');
    formGroup.style.position = 'relative';
    formGroup.appendChild(success);
    input.classList.add('success');
    input.style.borderColor = '#10b981';
}

function removeError(input) {
    const formGroup = input.closest('.form-group');
    const err = formGroup.querySelector('.field-error');
    const success = formGroup.querySelector('.field-success');
    if (err) err.remove();
    if (success) success.remove();
    input.classList.remove('error', 'success');
    input.style.borderColor = '';
}

function removeAllErrors() {
    document.querySelectorAll('.field-error').forEach(e => e.remove());
    document.querySelectorAll('.field-success').forEach(e => e.remove());
    document.querySelectorAll('.error').forEach(e => e.classList.remove('error'));
    document.querySelectorAll('.success').forEach(e => e.classList.remove('success'));
    document.querySelectorAll('input').forEach(input => {
        input.style.borderColor = '';
    });
}

function validateEmail() {
    const input = document.querySelector('input[name="email"]');
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
    const input = document.querySelector('input[name="phoneNumber"]');
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

function validatePassword() {
    const input = document.querySelector('input[name="password"]');
    const value = input.value.trim();
    
    // Xóa các class cũ
    input.classList.remove('error', 'success');
    removeError(input);
    
    if (!value) {
        showError(input, 'Mật khẩu là bắt buộc');
        return false;
    }
    
    // Kiểm tra từng quy tắc
    const hasMinLength = value.length >= 6;
    const hasLowercase = /[a-z]/.test(value);
    const hasUppercase = /[A-Z]/.test(value);
    const hasNumber = /\d/.test(value);
    
    // Tạo progress indicator
    const rules = [
        { check: hasMinLength, text: 'Ít nhất 6 ký tự' },
        { check: hasLowercase, text: 'Có chữ thường' },
        { check: hasUppercase, text: 'Có chữ hoa' },
        { check: hasNumber, text: 'Có số' }
    ];
    
    const completedRules = rules.filter(rule => rule.check).length;
    const totalRules = rules.length;
    
    if (completedRules === totalRules) {
        // Tất cả quy tắc đều đúng
        showSuccess(input, 'Mật khẩu hợp lệ');
        return true;
    } else {
        // Hiển thị tiến độ
        const message = `Mật khẩu: ${completedRules}/${totalRules} quy tắc (${rules.filter(r => !r.check).map(r => r.text).join(', ')} còn thiếu)`;
        showError(input, message);
        return false;
    }
} 