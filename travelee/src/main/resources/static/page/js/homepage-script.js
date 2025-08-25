// Homepage Script - Tất cả code được wrap trong DOMContentLoaded
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM loaded, initializing all features...');

    // Mobile Menu Toggle
    const hamburger = document.querySelector('.hamburger');
    const navMenu = document.querySelector('.nav-menu');
    const authButtons = document.querySelector('.auth-buttons');

    // Kiểm tra elements tồn tại trước khi thêm event listeners
    if (hamburger && navMenu && authButtons) {
        hamburger.addEventListener('click', function() {
            hamburger.classList.toggle('active');
            navMenu.classList.toggle('active');
            authButtons.classList.toggle('active');
        });

        // Close mobile menu when clicking on a link
        document.querySelectorAll('.nav-item a').forEach(link => {
            link.addEventListener('click', () => {
                hamburger.classList.remove('active');
                navMenu.classList.remove('active');
                authButtons.classList.remove('active');
            });
        });
    }

    // Smooth scrolling for navigation links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            const href = this.getAttribute('href');
            if (href === "#" || href === "#?" || href.length <= 1) return; // bỏ qua
            const target = document.querySelector(href);
            if (target) {
                e.preventDefault();
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });

    // Header scroll effect
    const header = document.querySelector('.header');
    if (header) {
        window.addEventListener('scroll', function() {
            if (window.scrollY > 100) {
                header.style.background = 'rgba(255, 255, 255, 0.98)';
                header.style.boxShadow = '0 2px 20px rgba(0, 0, 0, 0.15)';
            } else {
                header.style.background = 'rgba(255, 255, 255, 0.95)';
                header.style.boxShadow = '0 2px 20px rgba(0, 0, 0, 0.1)';
            }
        });
    }

    // Active navigation highlighting
    const sections = document.querySelectorAll('section[id]');
    const navItems = document.querySelectorAll('.nav-item');

    if (sections.length > 0 && navItems.length > 0) {
        window.addEventListener('scroll', function() {
            let current = '';
            sections.forEach(section => {
                const sectionTop = section.offsetTop;
                const sectionHeight = section.clientHeight;
                if (window.scrollY >= (sectionTop - 200)) {
                    current = section.getAttribute('id');
                }
            });

            navItems.forEach(item => {
                const link = item.querySelector('a');
                if (link) {
                    item.classList.remove('active');
                    if (link.getAttribute('href') === `#${current}`) {
                        item.classList.add('active');
                    }
                }
            });
        });
    }

    // Search functionality
    const searchBtn = document.querySelector('.btn-search');
    if (searchBtn) {
        searchBtn.addEventListener('click', function() {
            const destinationInput = document.querySelector('.search-input input[type="text"]');
            const dateInput = document.querySelector('.search-input input[type="date"]');
            const peopleSelect = document.querySelector('.search-input select');

            if (destinationInput && dateInput && peopleSelect) {
                const destination = destinationInput.value;
                const date = dateInput.value;
                const people = peopleSelect.value;

                if (destination && date && people !== 'Số người') {
                    alert(`Tìm kiếm tour đến ${destination} vào ngày ${date} cho ${people}`);
                    // Here you would typically send this data to your backend
                } else {
                    alert('Vui lòng điền đầy đủ thông tin tìm kiếm');
                }
            }
        });
    }

    // Newsletter subscription
    const subscribeBtn = document.querySelector('.btn-subscribe');
    if (subscribeBtn) {
        subscribeBtn.addEventListener('click', function () {
            const emailInput = document.querySelector('.newsletter-form input[type="email"]');
            if (emailInput) {
                const email = emailInput.value;
                const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

                if (email && emailRegex.test(email)) {
                    alert('Cảm ơn bạn đã đăng ký nhận tin tức!');
                    emailInput.value = '';
                } else {
                    alert('Vui lòng nhập email hợp lệ');
                }
            }
        });
    }

    // Tour booking buttons
    document.querySelectorAll('.btn-book').forEach(button => {
        button.addEventListener('click', function() {
            const tourCard = this.closest('.tour-card');
            if (tourCard) {
                const tourTitle = tourCard.querySelector('h3');
                const tourPrice = tourCard.querySelector('.tour-price');

                if (tourTitle && tourPrice) {
                    if (confirm(`Bạn có muốn đặt tour "${tourTitle.textContent}" với giá ${tourPrice.textContent}?`)) {
                        alert('Cảm ơn bạn đã đặt tour! Chúng tôi sẽ liên hệ sớm nhất.');
                    }
                }
            }
        });
    });

    // Animate elements on scroll
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };

    const observer = new IntersectionObserver(function(entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
            }
        });
    }, observerOptions);

    // Observe elements for animation
    document.querySelectorAll('.tour-card, .feature-card').forEach(el => {
        if (el) {
            el.style.opacity = '0';
            el.style.transform = 'translateY(30px)';
            el.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
            observer.observe(el);
        }
    });

    // Initialize counters when they come into view
    const counterObserver = new IntersectionObserver(function(entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const target = parseInt(entry.target.getAttribute('data-target'));
                animateCounter(entry.target, target);
                counterObserver.unobserve(entry.target);
            }
        });
    }, { threshold: 0.5 });

    // Enhanced search with suggestions
    const destinations = [
        'Đà Nẵng', 'Hội An', 'Sapa', 'Phú Quốc', 'Nha Trang',
        'Đà Lạt', 'Vũng Tàu', 'Mũi Né', 'Côn Đảo', 'Hạ Long'
    ];

    const searchInput = document.querySelector('.search-input input[type="text"]');
    if (searchInput) {
        searchInput.addEventListener('input', function() {
            const value = this.value.toLowerCase();
            const suggestions = destinations.filter(dest =>
                dest.toLowerCase().includes(value)
            );

            // You can implement a dropdown with suggestions here
            if (suggestions.length > 0 && value.length > 0) {
                console.log('Gợi ý:', suggestions);
            }
        });
    }

    // Video background fallback
    const heroVideo = document.querySelector('.hero-video');
    if (heroVideo) {
        heroVideo.addEventListener('error', function() {
            // Fallback to background image if video fails to load
            this.style.display = 'none';
            const hero = document.querySelector('.hero');
            if (hero) {
                hero.style.backgroundImage =
                    'url("https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=1920&q=80")';
                hero.style.backgroundSize = 'cover';
                hero.style.backgroundPosition = 'center';
            }
        });
    }

    // Performance optimization: Lazy loading for images
    if ('IntersectionObserver' in window) {
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

        document.querySelectorAll('img[data-src]').forEach(img => {
            imageObserver.observe(img);
        });
    }

    // Add some interactive effects
    document.querySelectorAll('.social-icon, .footer-social a').forEach(icon => {
        icon.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-3px) scale(1.1)';
        });

        icon.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0) scale(1)';
        });
    });

    // Smooth reveal animation for sections
    const revealSections = document.querySelectorAll('.featured-tours, .why-choose-us, .newsletter');
    revealSections.forEach(section => {
        if (section) {
            const sectionObserver = new IntersectionObserver(entries => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        entry.target.style.opacity = '1';
                        entry.target.style.transform = 'translateY(0)';
                    }
                });
            }, { threshold: 0.1 });

            section.style.opacity = '0';
            section.style.transform = 'translateY(50px)';
            section.style.transition = 'opacity 0.8s ease, transform 0.8s ease';
            sectionObserver.observe(section);
        }
    });

    // Test button
    const testBtn = document.getElementById('testBtn');
    if (testBtn) {
        testBtn.addEventListener('click', function() {
            alert('JavaScript is working!');
            console.log('Test button clicked');
        });
    }

    // Initialize chat widget
    initChatWidget();

    // Dropdown functionality
    document.querySelectorAll('.dropdown-toggle').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            this.parentElement.classList.toggle('show');
        });
    });

    // Close dropdowns when clicking outside
    window.addEventListener('click', function(e) {
        document.querySelectorAll('.dropdown').forEach(drop => {
            if (!drop.contains(e.target)) drop.classList.remove('show');
        });
    });
});

// Counter animation for statistics
function animateCounter(element, target, duration = 2000) {
    let start = 0;
    const increment = target / (duration / 16);

    function updateCounter() {
        start += increment;
        if (start < target) {
            element.textContent = Math.floor(start);
            requestAnimationFrame(updateCounter);
        } else {
            element.textContent = target;
        }
    }

    updateCounter();
}

// Form validation
function validateForm(form) {
    const inputs = form.querySelectorAll('input[required], select[required]');
    let isValid = true;

    inputs.forEach(input => {
        if (!input.value.trim()) {
            input.style.borderColor = '#ff4444';
            isValid = false;
        } else {
            input.style.borderColor = '#e1e5e9';
        }
    });

    return isValid;
}

// Add loading states to buttons
function addLoadingState(button) {
    const originalText = button.textContent;
    button.textContent = 'Đang xử lý...';
    button.disabled = true;

    setTimeout(() => {
        button.textContent = originalText;
        button.disabled = false;
    }, 2000);
}

// Chat Widget Functionality
function initChatWidget() {
    const chatButton = document.getElementById('chatButton');
    const chatPopup = document.getElementById('chatPopup');
    const closeChat = document.getElementById('closeChat');
    const messageInput = document.getElementById('messageInput');
    const sendMessage = document.getElementById('sendMessage');
    const chatMessages = document.getElementById('chatMessages');

    if (!chatButton || !chatPopup) return;

    // Toggle chat popup
    chatButton.addEventListener('click', function() {
        chatPopup.classList.toggle('active');
        if (chatPopup.classList.contains('active')) {
            messageInput.focus();
        }
    });

    // Close chat popup
    closeChat.addEventListener('click', function() {
        chatPopup.classList.remove('active');
    });

    // Send message function
    function sendUserMessage() {
        const message = messageInput.value.trim();
        if (message) {
            // Add user message
            const userMessageDiv = document.createElement('div');
            userMessageDiv.className = 'message user';
            userMessageDiv.innerHTML = `
                <div class="message-content">
                    <p>${message}</p>
                    <span class="time">Bây giờ</span>
                </div>
            `;
            chatMessages.appendChild(userMessageDiv);

            // Clear input
            messageInput.value = '';

            // Scroll to bottom
            chatMessages.scrollTop = chatMessages.scrollHeight;

            // Simulate agent response
            setTimeout(() => {
                const agentMessageDiv = document.createElement('div');
                agentMessageDiv.className = 'message agent';
                agentMessageDiv.innerHTML = `
                    <div class="message-content">
                        <p>Cảm ơn bạn đã liên hệ! Chúng tôi sẽ phản hồi sớm nhất có thể. Bạn có cần hỗ trợ thêm gì không? 😊</p>
                        <span class="time">Bây giờ</span>
                    </div>
                `;
                chatMessages.appendChild(agentMessageDiv);
                chatMessages.scrollTop = chatMessages.scrollHeight;
            }, 1000);
        }
    }

    // Send message on button click
    sendMessage.addEventListener('click', sendUserMessage);

    // Send message on Enter key
    messageInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            sendUserMessage();
        }
    });

    // Close chat when clicking outside
    document.addEventListener('click', function(e) {
        if (chatButton && chatPopup) {
            if (!chatButton.contains(e.target) && !chatPopup.contains(e.target)) {
                chatPopup.classList.remove('active');
            }
        }
    });
}