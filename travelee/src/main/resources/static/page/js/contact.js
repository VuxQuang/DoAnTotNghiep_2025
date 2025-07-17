// contact.js - Script riêng cho trang Liên hệ TravelGo

document.addEventListener('DOMContentLoaded', function() {
    // Tự động scroll lên đầu trang khi load
    window.scrollTo({ top: 0, behavior: 'smooth' });

    // Validate form liên hệ
    const form = document.querySelector('.contact-form');
    if (form) {
        form.addEventListener('submit', function(e) {
            let valid = true;
            const name = form.querySelector('#name');
            const email = form.querySelector('#email');
            const phone = form.querySelector('#phone');
            const message = form.querySelector('#message');

            // Reset border
            [name, email, phone, message].forEach(input => input.style.borderColor = '#e1e5e9');

            // Validate name
            if (!name.value.trim()) {
                name.style.borderColor = '#d32f2f';
                valid = false;
            }
            // Validate email
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!email.value.trim() || !emailRegex.test(email.value)) {
                email.style.borderColor = '#d32f2f';
                valid = false;
            }
            // Validate phone
            const phoneRegex = /^[0-9]{10,11}$/;
            if (!phone.value.trim() || !phoneRegex.test(phone.value)) {
                phone.style.borderColor = '#d32f2f';
                valid = false;
            }
            // Validate message
            if (!message.value.trim()) {
                message.style.borderColor = '#d32f2f';
                valid = false;
            }
            if (!valid) {
                e.preventDefault();
                alert('Vui lòng điền đầy đủ và đúng thông tin!');
                return false;
            }
        });
    }
}); 