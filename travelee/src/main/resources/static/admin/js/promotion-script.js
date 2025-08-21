// Promotion Management JavaScript

let deletePromotionId = null;

// Toggle dropdown menu
function toggleDropdown(button) {
    const dropdown = button.nextElementSibling;
    const isOpen = dropdown.classList.contains('show');
    
    // Close all other dropdowns
    document.querySelectorAll('.dropdown-menu.show').forEach(menu => {
        if (menu !== dropdown) {
            menu.classList.remove('show');
        }
    });
    
    // Toggle current dropdown
    dropdown.classList.toggle('show');
}

// Close dropdown when clicking outside
document.addEventListener('click', function(event) {
    if (!event.target.matches('.dropdown-toggle')) {
        document.querySelectorAll('.dropdown-menu.show').forEach(menu => {
            menu.classList.remove('show');
        });
    }
});

// Update promotion status
async function updateStatus(promotionId, status) {
    try {
        const response = await fetch(`/admin/promotions/${promotionId}/status`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `status=${status}`
        });
        
        if (response.ok) {
            const message = await response.text();
            showNotification(message, 'success');
            
            // Reload page after a short delay
            setTimeout(() => {
                window.location.reload();
            }, 1500);
        } else {
            const errorMessage = await response.text();
            showNotification(errorMessage, 'error');
        }
    } catch (error) {
        console.error('Error updating status:', error);
        showNotification('Có lỗi xảy ra khi cập nhật trạng thái', 'error');
    }
}

// Update expired promotions
async function updateExpiredPromotions() {
    try {
        const button = event.target;
        const originalText = button.innerHTML;
        
        // Show loading state
        button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang cập nhật...';
        button.disabled = true;
        
        const response = await fetch('/admin/promotions/update-expired', {
            method: 'POST'
        });
        
        if (response.ok) {
            const message = await response.text();
            showNotification(message, 'success');
            
            // Reload page after a short delay
            setTimeout(() => {
                window.location.reload();
            }, 1500);
        } else {
            const errorMessage = await response.text();
            showNotification(errorMessage, 'error');
        }
    } catch (error) {
        console.error('Error updating expired promotions:', error);
        showNotification('Có lỗi xảy ra khi cập nhật promotions hết hạn', 'error');
    } finally {
        // Restore button state
        button.innerHTML = originalText;
        button.disabled = false;
    }
}

// Delete promotion
function deletePromotion(promotionId) {
    deletePromotionId = promotionId;
    document.getElementById('deleteModal').style.display = 'block';
}

// Close delete modal
function closeDeleteModal() {
    document.getElementById('deleteModal').style.display = 'none';
    deletePromotionId = null;
}

// Confirm delete
async function confirmDelete() {
    if (!deletePromotionId) return;
    
    try {
        const response = await fetch(`/admin/promotions/delete/${deletePromotionId}`, {
            method: 'POST'
        });
        
        if (response.ok) {
            showNotification('Xóa mã giảm giá thành công!', 'success');
            closeDeleteModal();
            
            // Reload page after a short delay
            setTimeout(() => {
                window.location.reload();
            }, 1500);
        } else {
            const errorMessage = await response.text();
            showNotification(errorMessage, 'error');
        }
    } catch (error) {
        console.error('Error deleting promotion:', error);
        showNotification('Có lỗi xảy ra khi xóa mã giảm giá', 'error');
    }
}

// Close modal when clicking outside
window.onclick = function(event) {
    const modal = document.getElementById('deleteModal');
    if (event.target === modal) {
        closeDeleteModal();
    }
}

// Close modal when clicking close button
document.querySelector('.close').onclick = function() {
    closeDeleteModal();
}

// Show notification
function showNotification(message, type = 'info') {
    // Remove existing notifications
    const existingNotifications = document.querySelectorAll('.notification');
    existingNotifications.forEach(notification => notification.remove());
    
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.innerHTML = `
        <div class="notification-content">
            <i class="fas fa-${type === 'success' ? 'check-circle' : type === 'error' ? 'exclamation-circle' : 'info-circle'}"></i>
            <span>${message}</span>
        </div>
        <button class="notification-close" onclick="this.parentElement.remove()">
            <i class="fas fa-times"></i>
        </button>
    `;
    
    // Add styles
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: ${type === 'success' ? '#d4edda' : type === 'error' ? '#f8d7da' : '#d1ecf1'};
        color: ${type === 'success' ? '#155724' : type === 'error' ? '#721c24' : '#0c5460'};
        border: 1px solid ${type === 'success' ? '#c3e6cb' : type === 'error' ? '#f5c6cb' : '#bee5eb'};
        border-radius: 5px;
        padding: 15px 20px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        z-index: 10000;
        max-width: 400px;
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 15px;
        animation: slideInRight 0.3s ease;
    `;
    
    // Add animation keyframes
    if (!document.querySelector('#notification-styles')) {
        const style = document.createElement('style');
        style.id = 'notification-styles';
        style.textContent = `
            @keyframes slideInRight {
                from {
                    transform: translateX(100%);
                    opacity: 0;
                }
                to {
                    transform: translateX(0);
                    opacity: 1;
                }
            }
        `;
        document.head.appendChild(style);
    }
    
    // Add to page
    document.body.appendChild(notification);
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        if (notification.parentElement) {
            notification.remove();
        }
    }, 5000);
}

// Search functionality with debounce
let searchTimeout;
const searchInput = document.querySelector('.search-input');
if (searchInput) {
    searchInput.addEventListener('input', function() {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(() => {
            // Auto-submit search form
            this.closest('form').submit();
        }, 500);
    });
}

// Filter functionality
const filterSelects = document.querySelectorAll('.filter-select');
filterSelects.forEach(select => {
    select.addEventListener('change', function() {
        // Auto-submit form when filter changes
        this.closest('form').submit();
    });
});

// Sort functionality
const sortLinks = document.querySelectorAll('.data-table th a');
sortLinks.forEach(link => {
    link.addEventListener('click', function(e) {
        // Add loading indicator
        const icon = this.querySelector('i');
        if (icon) {
            icon.className = 'fas fa-spinner fa-spin';
        }
    });
});

// Table row hover effects
const tableRows = document.querySelectorAll('.data-table tbody tr');
tableRows.forEach(row => {
    row.addEventListener('mouseenter', function() {
        this.style.backgroundColor = '#f8f9fa';
    });
    
    row.addEventListener('mouseleave', function() {
        this.style.backgroundColor = '';
    });
});

// Usage bar animation
function animateUsageBars() {
    const usageBars = document.querySelectorAll('.usage-fill');
    usageBars.forEach(bar => {
        const width = bar.style.width;
        bar.style.width = '0%';
        
        setTimeout(() => {
            bar.style.width = width;
        }, 100);
    });
}

// Initialize animations when page loads
document.addEventListener('DOMContentLoaded', function() {
    // Animate usage bars
    animateUsageBars();
    
    // Add loading states to buttons
    const buttons = document.querySelectorAll('.btn');
    buttons.forEach(button => {
        button.addEventListener('click', function() {
            if (!this.disabled && !this.classList.contains('dropdown-toggle')) {
                this.style.opacity = '0.7';
                this.style.pointerEvents = 'none';
                
                setTimeout(() => {
                    this.style.opacity = '';
                    this.style.pointerEvents = '';
                }, 1000);
            }
        });
    });
    
    // Add smooth scrolling to pagination
    const paginationLinks = document.querySelectorAll('.pagination a');
    paginationLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            // Smooth scroll to top
            window.scrollTo({
                top: 0,
                behavior: 'smooth'
            });
        });
    });
});

// Keyboard shortcuts
document.addEventListener('keydown', function(e) {
    // Ctrl/Cmd + F to focus search
    if ((e.ctrlKey || e.metaKey) && e.key === 'f') {
        e.preventDefault();
        const searchInput = document.querySelector('.search-input');
        if (searchInput) {
            searchInput.focus();
            searchInput.select();
        }
    }
    
    // Escape to close modals
    if (e.key === 'Escape') {
        closeDeleteModal();
    }
});

// Export functionality (if needed)
function exportPromotions(format = 'csv') {
    // Implementation for exporting promotions data
    console.log(`Exporting promotions in ${format} format`);
    // Add your export logic here
}

// Print functionality
function printPromotions() {
    window.print();
}

// Add print styles
const printStyle = document.createElement('style');
printStyle.textContent = `
    @media print {
        .header-actions, .action-buttons, .pagination, .search-filter-container {
            display: none !important;
        }
        
        .data-table {
            border-collapse: collapse;
        }
        
        .data-table th,
        .data-table td {
            border: 1px solid #000;
            padding: 8px;
        }
    }
`;
document.head.appendChild(printStyle);
