// Review Management JavaScript

// Global variables
let currentReviewId = null;

// DOM Ready
document.addEventListener('DOMContentLoaded', function() {
    console.log('Review script loaded successfully');
    initializeModals();
    initializeFilters();
    initializeSearch();
    initializeTooltips();
});

// Initialize modals
function initializeModals() {
    const modals = document.querySelectorAll('.modal');
    const closeButtons = document.querySelectorAll('.close');
    
    // Close modal when clicking on close button
    closeButtons.forEach(button => {
        button.addEventListener('click', function() {
            closeModal();
        });
    });
    
    // Close modal when clicking outside
    modals.forEach(modal => {
        modal.addEventListener('click', function(e) {
            if (e.target === modal) {
                closeModal();
            }
        });
    });
}

// Initialize filters
function initializeFilters() {
    const filterForm = document.querySelector('.filter-form');
    if (filterForm) {
        filterForm.addEventListener('submit', function(e) {
            // Form will submit normally
        });
    }
}

// Initialize search
function initializeSearch() {
    const searchInput = document.getElementById('search');
    if (searchInput) {
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                this.form.submit();
            }
        });
    }
}

// Initialize tooltips
function initializeTooltips() {
    const tooltipElements = document.querySelectorAll('[title]');
    tooltipElements.forEach(element => {
        element.addEventListener('mouseenter', function() {
            // Add tooltip functionality if needed
        });
    });
}

// Delete review
function deleteReview(reviewId) {
    currentReviewId = reviewId;
    showModal('deleteModal');
}

// Show modal
function showModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'block';
        document.body.style.overflow = 'hidden';
    }
}

// Close modal
function closeModal() {
    const modals = document.querySelectorAll('.modal');
    modals.forEach(modal => {
        modal.style.display = 'none';
    });
    document.body.style.overflow = 'auto';
    currentReviewId = null;
}

// Proceed with delete
function proceedDelete() {
    if (currentReviewId) {
        // Create form and submit
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = `/admin/reviews/${currentReviewId}/delete`;
        
        // Add CSRF token if needed
        const csrfToken = document.querySelector('meta[name="_csrf"]');
        if (csrfToken) {
            const csrfInput = document.createElement('input');
            csrfInput.type = 'hidden';
            csrfInput.name = '_csrf';
            csrfInput.value = csrfToken.getAttribute('content');
            form.appendChild(csrfInput);
        }
        
        document.body.appendChild(form);
        form.submit();
    }
}


// Close modal with Escape key
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        closeModal();
    }
});

// Auto-hide alerts after 5 seconds
document.addEventListener('DOMContentLoaded', function() {
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.opacity = '0';
            alert.style.transition = 'opacity 0.5s ease';
            setTimeout(() => {
                alert.remove();
            }, 500);
        }, 5000);
    });
});

// Table row click to view detail
document.addEventListener('DOMContentLoaded', function() {
    const tableRows = document.querySelectorAll('.reviews-table tbody tr');
    tableRows.forEach(row => {
        row.addEventListener('click', function(e) {
            // Don't trigger if clicking on action buttons
            if (!e.target.closest('.action-buttons')) {
                const reviewId = this.querySelector('[data-review-id]')?.getAttribute('data-review-id');
                if (reviewId) {
                    window.location.href = `/admin/reviews/${reviewId}`;
                }
            }
        });
    });
});

// Export functions to global scope
window.deleteReview = deleteReview;
window.closeModal = closeModal;
window.proceedDelete = proceedDelete;
