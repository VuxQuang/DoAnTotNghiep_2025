

// === Biến global ===
let currentImageIndex = 0;
let tourImages = [];
let selectedScheduleId = null;

// === Khi trang load ===
document.addEventListener('DOMContentLoaded', function () {
    initializeImageGallery();
    initializeScheduleSelection();
    initializeSmoothScrolling();
    initializeAnimations();
    initializeLazyLoading();
    initializeScrollToTop();
});

// === Khởi tạo gallery ảnh ===
function initializeImageGallery() {
    const thumbnails = document.querySelectorAll('.thumbnail');
    const mainImage = document.getElementById('mainImage');
    const btnPrev = document.querySelector('.btn-prev');
    const btnNext = document.querySelector('.btn-next');

    // Lưu danh sách ảnh
    thumbnails.forEach((thumbnail, index) => {
        const img = thumbnail.querySelector('img');
        if (img) {
            tourImages.push(img.src);
        }
    });

    // Xử lý click thumbnail
    thumbnails.forEach((thumbnail, index) => {
        thumbnail.addEventListener('click', () => {
            changeImage(index);
        });
    });

    // Xử lý nút prev/next
    if (btnPrev) {
        btnPrev.addEventListener('click', () => {
            const prevIndex = currentImageIndex > 0 ? currentImageIndex - 1 : tourImages.length - 1;
            changeImage(prevIndex);
        });
    }

    if (btnNext) {
        btnNext.addEventListener('click', () => {
            const nextIndex = currentImageIndex < tourImages.length - 1 ? currentImageIndex + 1 : 0;
            changeImage(nextIndex);
        });
    }

    // Keyboard navigation
    document.addEventListener('keydown', (e) => {
        if (e.key === 'ArrowLeft') {
            const prevIndex = currentImageIndex > 0 ? currentImageIndex - 1 : tourImages.length - 1;
            changeImage(prevIndex);
        } else if (e.key === 'ArrowRight') {
            const nextIndex = currentImageIndex < tourImages.length - 1 ? currentImageIndex + 1 : 0;
            changeImage(nextIndex);
        }
    });
}

// === Thay đổi ảnh chính ===
function changeImage(index) {
    if (index < 0 || index >= tourImages.length) return;

    const mainImage = document.getElementById('mainImage');
    const thumbnails = document.querySelectorAll('.thumbnail');

    // Cập nhật ảnh chính
    if (mainImage) {
        mainImage.style.opacity = '0';
        setTimeout(() => {
            mainImage.src = tourImages[index];
            mainImage.style.opacity = '1';
        }, 150);
    }

    // Cập nhật thumbnail active
    thumbnails.forEach((thumbnail, i) => {
        thumbnail.classList.toggle('active', i === index);
    });

    currentImageIndex = index;
}

// === Khởi tạo chọn lịch khởi hành ===
function initializeScheduleSelection() {
    const scheduleButtons = document.querySelectorAll('.btn-select-date');
    
    scheduleButtons.forEach(button => {
        button.addEventListener('click', function() {
            if (this.disabled) return;
            
            // Bỏ chọn tất cả
            scheduleButtons.forEach(btn => {
                btn.classList.remove('selected');
                btn.style.background = '#667eea';
            });
            
            // Chọn button hiện tại
            this.classList.add('selected');
            this.style.background = '#27ae60';
            
            // Lưu schedule ID từ data attribute
            const scheduleId = this.getAttribute('data-schedule-id');
            if (scheduleId) {
                selectedScheduleId = scheduleId;
            }
        });
    });
}

// === Chọn lịch khởi hành ===
function selectSchedule(scheduleId) {
    selectedScheduleId = scheduleId;
    
    // Cập nhật UI
    const scheduleButtons = document.querySelectorAll('.btn-select-date');
    scheduleButtons.forEach(button => {
        button.classList.remove('selected');
        button.style.background = '#667eea';
    });
    
    const selectedButton = document.querySelector(`[onclick="selectSchedule(${scheduleId})"]`);
    if (selectedButton) {
        selectedButton.classList.add('selected');
        selectedButton.style.background = '#27ae60';
        
        // Cập nhật giá hiển thị theo giá khuyến mãi của schedule
        const promotionalPrice = selectedButton.getAttribute('data-promotional-price');
        if (promotionalPrice) {
            updateDisplayedPrice(promotionalPrice);
        }
    }
    
    // Hiển thị thông báo đã chọn
    showNotification('Đã chọn lịch khởi hành! Bạn có thể bấm "Đặt tour ngay" để tiếp tục.', 'success');
    
    // Scroll đến form đặt tour
    const priceCard = document.querySelector('.price-card');
    if (priceCard) {
        priceCard.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
}

// === Cập nhật giá hiển thị ===
function updateDisplayedPrice(promotionalPrice) {
    const priceValue = document.querySelector('.price-adult .price-value');
    if (priceValue) {
        const formattedPrice = new Intl.NumberFormat('vi-VN').format(promotionalPrice);
        priceValue.textContent = formattedPrice + '₫';
        
        // Thêm hiệu ứng highlight
        priceValue.style.color = '#27ae60';
        priceValue.style.fontWeight = 'bold';
        setTimeout(() => {
            priceValue.style.color = '';
            priceValue.style.fontWeight = '';
        }, 2000);
    }
}

// === Khởi tạo smooth scrolling ===
function initializeSmoothScrolling() {
    const links = document.querySelectorAll('a[href^="#"]');
    
    links.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const targetId = this.getAttribute('href').substring(1);
            const targetElement = document.getElementById(targetId);
            
            if (targetElement) {
                targetElement.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
}

// === Khởi tạo animations ===
function initializeAnimations() {
    // Intersection Observer cho fade-in animations
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };
    
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('fade-in');
            }
        });
    }, observerOptions);
    
    // Observe các elements cần animation
    const animatedElements = document.querySelectorAll('.tour-section, .price-card, .schedule-card, .contact-card');
    animatedElements.forEach(el => {
        observer.observe(el);
    });
}

// === Format giá tiền ===
function formatPrice(price) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(price);
}

// === Format ngày tháng ===
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
    });
}

// === Xử lý đặt tour với Controller ===
function bookTourWithSchedule() {
    if (!selectedScheduleId) {
        showNotification('Vui lòng chọn lịch khởi hành trước khi đặt tour!', 'error');
        return;
    }
    
    // Chuyển hướng đến form booking với scheduleId
    const tourId = getTourIdFromUrl();
    const bookingUrl = `/page/booking/${tourId}?scheduleId=${selectedScheduleId}`;
    window.location.href = bookingUrl;
}

// === Xử lý đặt tour (giữ lại để tương thích) ===
function bookTour() {
    bookTourWithController();
}

// === Lấy tour ID từ URL (Thymeleaf) ===
function getTourIdFromUrl() {
    // Lấy tour ID từ URL path (ví dụ: /page/tours/1)
    const pathSegments = window.location.pathname.split('/');
    const tourId = pathSegments[pathSegments.length - 1];
    return tourId;
}

// === Lazy loading cho ảnh ===
function initializeLazyLoading() {
    const images = document.querySelectorAll('img[data-src]');
    
    const imageObserver = new IntersectionObserver((entries, observer) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const img = entry.target;
                img.src = img.dataset.src;
                img.classList.remove('lazy');
                imageObserver.unobserve(img);
            }
        });
    });
    
    images.forEach(img => imageObserver.observe(img));
}

// === Xử lý responsive menu ===
function toggleMobileMenu() {
    const menu = document.querySelector('.mobile-menu');
    if (menu) {
        menu.classList.toggle('active');
    }
}

// === Share tour ===
function shareTour() {
    const tourTitle = document.querySelector('.tour-title h1')?.textContent || 'Tour du lịch';
    const currentUrl = window.location.href;
    
    if (navigator.share) {
        navigator.share({
            title: tourTitle,
            url: currentUrl
        });
    } else {
        // Fallback: copy to clipboard
        navigator.clipboard.writeText(currentUrl).then(() => {
            showNotification('Đã sao chép link tour vào clipboard!', 'success');
        });
    }
}

// === Add to favorites ===
function addToFavorites() {
    const tourId = getTourIdFromUrl();
    const favoriteBtn = document.querySelector('.btn-favorite');
    
    if (favoriteBtn) {
        favoriteBtn.classList.toggle('active');
        const isFavorite = favoriteBtn.classList.contains('active');
        
        // Lưu vào localStorage
        const favorites = JSON.parse(localStorage.getItem('favorites') || '[]');
        if (isFavorite) {
            if (!favorites.includes(tourId)) {
                favorites.push(tourId);
            }
        } else {
            const index = favorites.indexOf(tourId);
            if (index > -1) {
                favorites.splice(index, 1);
            }
        }
        localStorage.setItem('favorites', JSON.stringify(favorites));
        
        // Hiển thị thông báo
        const message = isFavorite ? 'Đã thêm vào yêu thích!' : 'Đã xóa khỏi yêu thích!';
        showNotification(message, isFavorite ? 'success' : 'info');
    }
}

// === Hiển thị thông báo ===
function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;
    
    // Style cho notification
    const backgroundColor = type === 'success' ? '#27ae60' : 
                          type === 'error' ? '#e74c3c' : '#667eea';
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: ${backgroundColor};
        color: white;
        padding: 12px 20px;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        z-index: 1000;
        transform: translateX(100%);
        transition: transform 0.3s ease;
        font-family: 'Poppins', sans-serif;
        font-weight: 500;
    `;
    
    document.body.appendChild(notification);
    
    // Animation in
    setTimeout(() => {
        notification.style.transform = 'translateX(0)';
    }, 100);
    
    // Auto remove
    setTimeout(() => {
        notification.style.transform = 'translateX(100%)';
        setTimeout(() => {
            if (document.body.contains(notification)) {
                document.body.removeChild(notification);
            }
        }, 300);
    }, 3000);
}

// === Smooth scroll to top ===
function scrollToTop() {
    window.scrollTo({
        top: 0,
        behavior: 'smooth'
    });
}

// === Khởi tạo scroll to top button ===
function initializeScrollToTop() {
    // Thêm CSS cho animations
    const style = document.createElement('style');
    style.textContent = `
        .fade-in {
            animation: fadeIn 0.6s ease-in-out;
        }
        
        @keyframes fadeIn {
            from {
                opacity: 0;
                transform: translateY(20px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
        
        .btn-select-date.selected {
            background: #27ae60 !important;
        }
        
        .scroll-to-top {
            position: fixed;
            bottom: 30px;
            right: 30px;
            width: 50px;
            height: 50px;
            border-radius: 50%;
            background: #667eea;
            color: white;
            border: none;
            cursor: pointer;
            font-size: 1.2rem;
            box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
            transition: all 0.3s;
            opacity: 0;
            visibility: hidden;
            z-index: 1000;
        }
        
        .scroll-to-top:hover {
            transform: scale(1.1);
            background: #4b3ecf;
        }
    `;
    document.head.appendChild(style);
    
    // Thêm scroll to top button
    const scrollToTopBtn = document.createElement('button');
    scrollToTopBtn.innerHTML = '<i class="fas fa-arrow-up"></i>';
    scrollToTopBtn.className = 'scroll-to-top';
    
    scrollToTopBtn.addEventListener('click', scrollToTop);
    document.body.appendChild(scrollToTopBtn);
    
    // Show/hide scroll to top button
    window.addEventListener('scroll', () => {
        if (window.pageYOffset > 300) {
            scrollToTopBtn.style.opacity = '1';
            scrollToTopBtn.style.visibility = 'visible';
        } else {
            scrollToTopBtn.style.opacity = '0';
            scrollToTopBtn.style.visibility = 'hidden';
        }
    });
}