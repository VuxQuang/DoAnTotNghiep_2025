// Home Search Bar JavaScript
document.addEventListener('DOMContentLoaded', function() {
    initializeHomeSearch();
});

function initializeHomeSearch() {
    const form = document.getElementById('homeSearchForm');
    const searchBtn = document.querySelector('.search-btn');

    if (!form) return;

    // Set min date to today
    const dateInput = document.getElementById('homeDepartureDate');
    if (dateInput) {
        const today = new Date().toISOString().split('T')[0];
        dateInput.min = today;
    }

    // Form submission
    form.addEventListener('submit', function(e) {
        if (!validateHomeSearch()) {
            e.preventDefault();
            showHomeSearchError('Vui lòng kiểm tra lại thông tin tìm kiếm');
        } else {
            // Show loading state
            searchBtn.classList.add('loading');

            // Simulate loading delay (remove in production)
            setTimeout(() => {
                searchBtn.classList.remove('loading');
            }, 2000);
        }
    });

    // Auto-focus on keyword input
    const keywordInput = document.getElementById('homeKeyword');
    if (keywordInput) {
        keywordInput.focus();

        // Add animation delay for each field
        const fields = form.querySelectorAll('.search-field');
        fields.forEach((field, index) => {
            field.style.setProperty('--animation-order', index);
        });
    }

    // Real-time validation
    if (keywordInput) {
        keywordInput.addEventListener('input', function() {
            validateKeywordField(this);
        });
    }

    if (dateInput) {
        dateInput.addEventListener('change', function() {
            validateDateField(this);
        });
    }
}

// Validate home search form
function validateHomeSearch() {
    const keyword = document.getElementById('homeKeyword');
    const departureDate = document.getElementById('homeDepartureDate');
    const priceRange = document.getElementById('homePriceRange');

    let isValid = true;

    // Clear previous errors
    clearHomeSearchErrors();

    // Validate keyword (optional but recommended)
    if (keyword && keyword.value.trim().length > 0) {
        if (keyword.value.trim().length < 2) {
            showFieldError(keyword, 'Từ khóa phải có ít nhất 2 ký tự');
            isValid = false;
        }
    }

    // Validate date (optional)
    if (departureDate && departureDate.value) {
        const selectedDate = new Date(departureDate.value);
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        if (selectedDate < today) {
            showFieldError(departureDate, 'Ngày khởi hành không thể trong quá khứ');
            isValid = false;
        }
    }

    return isValid;
}

// Validate keyword field
function validateKeywordField(field) {
    const value = field.value.trim();

    if (value.length > 0 && value.length < 2) {
        showFieldError(field, 'Từ khóa phải có ít nhất 2 ký tự');
    } else {
        clearFieldError(field);
    }
}

// Validate date field
function validateDateField(field) {
    if (field.value) {
        const selectedDate = new Date(field.value);
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        if (selectedDate < today) {
            showFieldError(field, 'Ngày khởi hành không thể trong quá khứ');
        } else {
            clearFieldError(field);
        }
    }
}

// Show field error
function showFieldError(field, message) {
    clearFieldError(field);

    const errorSpan = document.createElement('span');
    errorSpan.className = 'field-error';
    errorSpan.style.cssText = `
        color: #ef4444;
        font-size: 0.8rem;
        margin-top: 5px;
        display: block;
        animation: fadeIn 0.3s ease-in;
        font-weight: 500;
    `;
    errorSpan.textContent = message;

    // Add CSS animation
    const style = document.createElement('style');
    style.textContent = `
        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(-5px); }
            to { opacity: 1; transform: translateY(0); }
        }
    `;
    document.head.appendChild(style);

    field.parentNode.appendChild(errorSpan);

    // Highlight field
    field.style.borderColor = '#ef4444';
    field.style.boxShadow = '0 0 0 3px rgba(239, 68, 68, 0.1)';
}

// Clear field error
function clearFieldError(field) {
    const existingError = field.parentNode.querySelector('.field-error');
    if (existingError) {
        existingError.remove();
    }

    field.style.borderColor = '';
    field.style.boxShadow = '';
}

// Clear all errors
function clearHomeSearchErrors() {
    const errors = document.querySelectorAll('.field-error');
    errors.forEach(error => error.remove());

    const fields = document.querySelectorAll('.search-field input, .search-field select');
    fields.forEach(field => {
        field.style.borderColor = '';
        field.style.boxShadow = '';
    });
}

// Show general error message
function showHomeSearchError(message) {
    const existingError = document.querySelector('.home-search-error');
    if (existingError) {
        existingError.remove();
    }

    const errorDiv = document.createElement('div');
    errorDiv.className = 'home-search-error';
    errorDiv.style.cssText = `
        background: #ef4444;
        color: white;
        padding: 15px;
        border-radius: 10px;
        margin: 15px 0;
        text-align: center;
        font-weight: 600;
        animation: shake 0.5s ease-in-out;
        box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
    `;
    errorDiv.textContent = message;

    // Add CSS animation
    const style = document.createElement('style');
    style.textContent = `
        @keyframes shake {
            0%, 100% { transform: translateX(0); }
            25% { transform: translateX(-5px); }
            75% { transform: translateX(5px); }
        }
    `;
    document.head.appendChild(style);

    const form = document.getElementById('homeSearchForm');
    form.insertBefore(errorDiv, form.firstChild);

    // Auto hide after 5 seconds
    setTimeout(() => {
        if (errorDiv.parentNode) {
            errorDiv.remove();
        }
    }, 5000);
}

// Export functions for external use
window.HomeSearch = {
    validateHomeSearch,
    showHomeSearchError,
    clearHomeSearchErrors
};
