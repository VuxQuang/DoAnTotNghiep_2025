// Xử lý hiển thị/ẩn mật khẩu
document.addEventListener('DOMContentLoaded', function() {
    const togglePassword = document.querySelector('.toggle-password');
    const passwordInput = document.querySelector('input[name="password"]');
    
    if (togglePassword && passwordInput) {
        togglePassword.addEventListener('click', function() {
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordInput.setAttribute('type', type);
            
            // Thay đổi icon
            this.classList.toggle('fa-eye');
            this.classList.toggle('fa-eye-slash');
        });
    }
    
    // Hiệu ứng loading khi submit form
    const loginForm = document.querySelector('.login-form');
    const loginBtn = document.querySelector('.login-btn');
    
    if (loginForm && loginBtn) {
        loginForm.addEventListener('submit', function(e) {
            // Thêm class loading
            loginBtn.classList.add('loading');
            
            // Mô phỏng delay (có thể xóa trong thực tế)
            setTimeout(() => {
                loginBtn.classList.remove('loading');
            }, 2000);
        });
    }
    
    // Hiệu ứng focus cho input
    const inputs = document.querySelectorAll('.input-icon input');
    inputs.forEach(input => {
        input.addEventListener('focus', function() {
            this.parentElement.classList.add('focused');
        });
        
        input.addEventListener('blur', function() {
            this.parentElement.classList.remove('focused');
        });
    });
}); 