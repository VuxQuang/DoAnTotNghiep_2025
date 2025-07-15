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

    // Validate realtime khi blur
    form.querySelectorAll('input').forEach(input => {
        input.addEventListener('blur', function() {
            removeError(this);
            switch (this.name) {
                case 'email': validateEmail(); break;
                case 'phone': validatePhone(); break;
                case 'password': validatePassword(); break;
            }
        });
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
    const input = document.querySelector('input[name="phone"]');
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