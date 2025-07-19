// Validate đổi mật khẩu (giả sử form có id là 'changePasswordForm' và input mật khẩu mới có id 'newPassword')
document.addEventListener('DOMContentLoaded', function() {
    var changePasswordForm = document.getElementById('changePasswordForm');
    if (changePasswordForm) {
        changePasswordForm.addEventListener('submit', function(e) {
            var newPassword = document.getElementById('newPassword').value;
            var passwordError = document.getElementById('passwordError');
            // Regex: ít nhất 1 chữ hoa, 1 số, tối thiểu 6 ký tự
            var passwordRegex = /^(?=.*[A-Z])(?=.*\d).{6,}$/;
            if (!passwordRegex.test(newPassword)) {
                e.preventDefault();
                if (!passwordError) {
                    passwordError = document.createElement('div');
                    passwordError.id = 'passwordError';
                    passwordError.style.color = '#d32f2f';
                    passwordError.style.marginTop = '8px';
                    document.getElementById('newPassword').parentNode.appendChild(passwordError);
                }
                passwordError.textContent = 'Mật khẩu mới phải có ít nhất 1 chữ hoa, 1 chữ số và tối thiểu 6 ký tự.';
                document.getElementById('newPassword').style.borderColor = '#d32f2f';
            } else {
                if (passwordError) passwordError.textContent = '';
                document.getElementById('newPassword').style.borderColor = '#e1e5e9';
            }
        });
    }
});
document.addEventListener("DOMContentLoaded", function () {
    const menuItems = document.querySelectorAll(".profile-menu-item");
    const tabs = document.querySelectorAll(".profile-tab");

    menuItems.forEach(item => {
        item.addEventListener("click", function () {
            const tabName = this.dataset.tab;

            // Remove active từ tất cả
            menuItems.forEach(i => i.classList.remove("active"));
            tabs.forEach(tab => tab.classList.remove("active"));

            // Gán active mới
            this.classList.add("active");
            const targetTab = document.querySelector(`.profile-tab.profile-${tabName}`);
            if (targetTab) targetTab.classList.add("active");
        });
    });
});
document.addEventListener("DOMContentLoaded", function () {
    const urlParams = new URLSearchParams(window.location.search);
    const tab = urlParams.get("tab");

    if (tab) {
        const activeTabBtn = document.querySelector(`.profile-menu-item[data-tab="${tab}"]`);
        if (activeTabBtn) activeTabBtn.click();
    }
});
document.querySelectorAll('.toggle-password').forEach(function(eye) {
    eye.addEventListener('click', function() {
        const targetId = this.getAttribute('data-target');
        const input = document.getElementById(targetId);
        if (input) {
            if (input.type === 'password') {
                input.type = 'text';
                this.querySelector('i').classList.remove('fa-eye');
                this.querySelector('i').classList.add('fa-eye-slash');
            } else {
                input.type = 'password';
                this.querySelector('i').classList.remove('fa-eye-slash');
                this.querySelector('i').classList.add('fa-eye');
            }
        }
    });
});

    
window.addEventListener('click', function(e) {
    document.querySelectorAll('.dropdown').forEach(drop => {
        if (!drop.contains(e.target)) drop.classList.remove('show');
    });
});

function toggleReply(index) {
    const elem = document.getElementById("reply-" + index);
    if (elem.style.display === "none" || elem.style.display === "") {
    elem.style.display = "block";
} else {
    elem.style.display = "none";
}
}

