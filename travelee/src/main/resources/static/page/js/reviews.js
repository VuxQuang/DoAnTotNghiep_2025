// Reviews JavaScript - Thymeleaf Version
document.addEventListener('DOMContentLoaded', function() {
    console.log('Reviews script loaded');
    
    // Initialize rating input
    setupRatingInput();
    
    // Setup review form
    setupReviewForm();
});

// Setup rating input functionality
function setupRatingInput() {
    const stars = document.querySelectorAll('.rating-input .stars i');
    const ratingInput = document.getElementById('rating');
    const ratingNumber = document.querySelector('.rating-number');
    
    console.log('Setting up rating input:', { stars: stars.length, ratingInput: !!ratingInput, ratingNumber: !!ratingNumber });
    
    if (!stars.length || !ratingInput) {
        console.log('Rating elements not found');
        return;
    }
    
    stars.forEach((star, index) => {
        star.addEventListener('click', function(e) {
            e.preventDefault();
            const rating = parseInt(this.getAttribute('data-rating'));
            console.log('Star clicked:', rating);
            setRating(rating);
        });
        
        star.addEventListener('mouseenter', function() {
            const rating = parseInt(this.getAttribute('data-rating'));
            highlightStars(rating);
        });
    });
    
    // Reset stars on mouse leave
    const ratingContainer = document.querySelector('.rating-input .stars');
    if (ratingContainer) {
        ratingContainer.addEventListener('mouseleave', function() {
            const currentRating = parseInt(ratingInput.value) || 0;
            highlightStars(currentRating);
        });
    }
}

// Set rating value
function setRating(rating) {
    const ratingInput = document.getElementById('rating');
    const ratingNumber = document.querySelector('.rating-number');
    
    console.log('Setting rating:', rating);
    
    if (ratingInput) {
        ratingInput.value = rating;
        console.log('Rating input updated:', ratingInput.value);
    }
    
    if (ratingNumber) {
        ratingNumber.textContent = rating;
        console.log('Rating number updated:', ratingNumber.textContent);
    }
    
    highlightStars(rating);
}

// Highlight stars based on rating
function highlightStars(rating) {
    const stars = document.querySelectorAll('.rating-input .stars i');
    
    console.log('Highlighting stars for rating:', rating, 'Found stars:', stars.length);
    
    stars.forEach((star, index) => {
        const starRating = parseInt(star.getAttribute('data-rating'));
        
        if (starRating <= rating) {
            star.className = 'fas fa-star';
        } else {
            star.className = 'far fa-star';
        }
    });
}

// Setup review form
function setupReviewForm() {
    const reviewForm = document.querySelector('.review-form');
    
    if (!reviewForm) return;
    
    // Add form validation
    reviewForm.addEventListener('submit', function(e) {
        const rating = parseInt(document.getElementById('rating').value);
        const comment = document.getElementById('comment').value.trim();
        
        if (rating === 0) {
            e.preventDefault();
            alert('Vui lòng chọn đánh giá từ 1 đến 5 sao');
            return false;
        }
        
        if (!comment) {
            e.preventDefault();
            alert('Vui lòng nhập nhận xét');
            return false;
        }
        
        // Form will submit normally to Thymeleaf controller
        return true;
    });
}

// Form is always visible now, no need for show/hide functions

// Reset review form
function resetReviewForm() {
    const ratingInput = document.getElementById('rating');
    const commentInput = document.getElementById('comment');
    
    if (ratingInput) {
        ratingInput.value = '0';
    }
    
    if (commentInput) {
        commentInput.value = '';
    }
    
    // Reset stars display
    highlightStars(0);
    
    const ratingNumber = document.querySelector('.rating-number');
    if (ratingNumber) {
        ratingNumber.textContent = '0';
    }
}

// Auto-hide flash messages after 5 seconds and reset form on success
document.addEventListener('DOMContentLoaded', function() {
    const alerts = document.querySelectorAll('.alert');
    
    alerts.forEach(alert => {
        // Check if it's a success message
        if (alert.classList.contains('alert-success')) {
            // Reset form on success
            resetReviewForm();
        }
        
        setTimeout(() => {
            alert.style.opacity = '0';
            alert.style.transition = 'opacity 0.5s ease';
            
            setTimeout(() => {
                alert.remove();
            }, 500);
        }, 5000);
    });
});