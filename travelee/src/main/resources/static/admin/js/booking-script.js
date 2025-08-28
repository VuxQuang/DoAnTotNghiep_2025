// Booking Management JavaScript

// Debug: Kiểm tra script có được load không
console.log('Booking script loaded successfully');

// Global variables
let currentBookingId = null;
let currentAction = null;

// Đảm bảo các function được định nghĩa trước khi sử dụng
window.confirmBooking = confirmBooking;
window.cancelBooking = cancelBooking;
window.completeBooking = completeBooking;
window.showRefundModal = showRefundModal;
window.markAsPaid = markAsPaid;
window.proceedMarkAsPaid = proceedMarkAsPaid;

// DOM Ready
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM loaded, initializing booking functions...');
    initializeModals();
    initializeFilters();
    initializeSearch();
    initializeDateRangePicker();
    initializeTooltips();
    
    // Debug: Kiểm tra các function có được định nghĩa không
    console.log('confirmBooking function:', typeof confirmBooking);
    console.log('cancelBooking function:', typeof cancelBooking);
    console.log('completeBooking function:', typeof completeBooking);
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
    
    // Close modal with Escape key
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            closeModal();
        }
    });
}

// Initialize filters
function initializeFilters() {
    const filterForm = document.querySelector('.filter-form');
    if (filterForm) {
        const inputs = filterForm.querySelectorAll('input, select');
        inputs.forEach(input => {
            input.addEventListener('change', function() {
                // Auto-submit form when filter changes
                if (input.type !== 'text') {
                    filterForm.submit();
                }
            });
        });
    }
}

// Initialize search
function initializeSearch() {
    const searchInput = document.getElementById('search');
    if (searchInput) {
        let searchTimeout;
        searchInput.addEventListener('input', function() {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                document.querySelector('.filter-form').submit();
            }, 500);
        });
    }
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
    
    // Reset form fields
    const cancelReason = document.getElementById('cancelReason');
    if (cancelReason) {
        cancelReason.value = '';
    }
    
    // Reset global variables
    currentBookingId = null;
    currentAction = null;
}

// Confirm booking
function confirmBooking(bookingId) {
    currentBookingId = bookingId;
    currentAction = 'confirm';
    showModal('confirmModal');
}

// Complete booking
function completeBooking(bookingId) {
    currentBookingId = bookingId;
    currentAction = 'complete';
    showModal('completeModal');
}

// Cancel booking
function cancelBooking(bookingId) {
    currentBookingId = bookingId;
    currentAction = 'cancel';
    showModal('cancelModal');
}

// Refund booking
function showRefundModal(bookingId) {
    currentBookingId = bookingId;
    currentAction = 'refund';
    showModal('refundModal');
}

// Mark booking as paid
function markAsPaid(bookingId) {
    currentBookingId = bookingId;
    currentAction = 'markAsPaid';
    showModal('paidModal');
}

// Proceed with confirm action
function proceedConfirm() {
    if (currentBookingId && currentAction === 'confirm') {
        updateBookingStatus(currentBookingId, 'CONFIRMED');
    }
}

// Proceed with mark as paid action
function proceedMarkAsPaid() {
    if (currentBookingId && currentAction === 'markAsPaid') {
        updateBookingStatus(currentBookingId, 'PAID');
    }
}

// Proceed with complete action
function proceedComplete() {
    if (currentBookingId && currentAction === 'complete') {
        updateBookingStatus(currentBookingId, 'COMPLETED');
    }
}

// Proceed with cancel action
function proceedCancel() {
    if (currentBookingId && currentAction === 'cancel') {
        const reason = document.getElementById('cancelReason').value.trim();
        if (!reason) {
            showAlert('Vui lòng nhập lý do hủy tour!', 'error');
            return;
        }
        updateBookingStatus(currentBookingId, 'CANCELLED', reason);
    }
}

// Proceed with refund action
function proceedRefund() {
    if (currentBookingId && currentAction === 'refund') {
        const amountStr = document.getElementById('refundAmount').value.trim();
        const reason = document.getElementById('refundReason').value.trim();
        if (!amountStr || isNaN(Number(amountStr)) || Number(amountStr) <= 0) {
            showAlert('Vui lòng nhập số tiền hoàn hợp lệ!', 'error');
            return;
        }
        if (!reason) {
            showAlert('Vui lòng nhập lý do hoàn tiền!', 'error');
            return;
        }

        const formData = new FormData();
        formData.append('amount', amountStr);
        formData.append('reason', reason);

        fetch(`/admin/bookings/${currentBookingId}/refund`, {
            method: 'POST',
            body: formData
        })
        .then(response => {
            if (response.ok) {
                return response.text();
            }
            throw new Error('Network response was not ok');
        })
        .then(() => {
            showAlert('Hoàn tiền thành công!', 'success');
            setTimeout(() => window.location.reload(), 1500);
        })
        .catch(error => {
            console.error('Error:', error);
            showAlert('Có lỗi xảy ra khi hoàn tiền!', 'error');
        })
        .finally(() => closeModal());
    }
}

// Update booking status
function updateBookingStatus(bookingId, status, reason = null) {
    const formData = new FormData();
    formData.append('status', status);
    if (reason) {
        formData.append('reason', reason);
    }
    
    fetch(`/admin/bookings/${bookingId}/status`, {
        method: 'POST',
        body: formData
    })
    .then(response => {
        if (response.ok) {
            return response.text();
        }
        throw new Error('Network response was not ok');
    })
    .then(html => {
        // Parse the response and extract the success/error message
        const parser = new DOMParser();
        const doc = parser.parseFromString(html, 'text/html');
        const successMessage = doc.querySelector('.alert-success span');
        const errorMessage = doc.querySelector('.alert-danger span');
        
        if (successMessage) {
            showAlert(successMessage.textContent, 'success');
            // Reload page after successful update
            setTimeout(() => {
                window.location.reload();
            }, 1500);
        } else if (errorMessage) {
            showAlert(errorMessage.textContent, 'error');
        } else {
            showAlert('Cập nhật trạng thái thành công!', 'success');
            setTimeout(() => {
                window.location.reload();
            }, 1500);
        }
        
        closeModal();
    })
    .catch(error => {
        console.error('Error:', error);
        showAlert('Có lỗi xảy ra khi cập nhật trạng thái!', 'error');
        closeModal();
    });
}

// Show alert message
function showAlert(message, type) {
    // Remove existing alerts
    const existingAlerts = document.querySelectorAll('.alert');
    existingAlerts.forEach(alert => alert.remove());
    
    // Create new alert
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type}`;
    alertDiv.innerHTML = `
        <i class="fas fa-${type === 'success' ? 'check-circle' : 'exclamation-circle'}"></i>
        <span>${message}</span>
    `;
    
    // Insert alert at the top of content
    const content = document.querySelector('.content');
    if (content) {
        content.insertBefore(alertDiv, content.firstChild);
        
        // Auto-remove alert after 5 seconds
        setTimeout(() => {
            alertDiv.remove();
        }, 5000);
    }
}

// Export booking data to CSV
function exportBookings() {
    const table = document.querySelector('.bookings-table');
    if (!table) return;
    
    let csv = [];
    const rows = table.querySelectorAll('tr');
    
    for (let i = 0; i < rows.length; i++) {
        let row = [], cols = rows[i].querySelectorAll('td, th');
        
        for (let j = 0; j < cols.length; j++) {
            // Get text content and clean it
            let text = cols[j].innerText.replace(/"/g, '""');
            row.push('"' + text + '"');
        }
        
        csv.push(row.join(','));
    }
    
    // Download CSV file
    const csvContent = csv.join('\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', 'bookings.csv');
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

// Print booking details
function printBooking() {
    window.print();
}

// Send email notification
function sendEmailNotification(bookingId, type) {
    fetch(`/admin/bookings/${bookingId}/send-email`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            type: type
        })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showAlert('Email đã được gửi thành công!', 'success');
        } else {
            showAlert('Có lỗi xảy ra khi gửi email!', 'error');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showAlert('Có lỗi xảy ra khi gửi email!', 'error');
    });
}

// Bulk actions
function selectAllBookings() {
    const checkboxes = document.querySelectorAll('.booking-checkbox');
    const selectAllCheckbox = document.getElementById('selectAll');
    
    checkboxes.forEach(checkbox => {
        checkbox.checked = selectAllCheckbox.checked;
    });
    
    updateBulkActionButtons();
}

function updateBulkActionButtons() {
    const selectedBookings = document.querySelectorAll('.booking-checkbox:checked');
    const bulkActionButtons = document.querySelectorAll('.bulk-action-btn');
    
    bulkActionButtons.forEach(button => {
        button.disabled = selectedBookings.length === 0;
    });
}

function bulkUpdateStatus(status) {
    const selectedBookings = document.querySelectorAll('.booking-checkbox:checked');
    const bookingIds = Array.from(selectedBookings).map(checkbox => checkbox.value);
    
    if (bookingIds.length === 0) {
        showAlert('Vui lòng chọn ít nhất một booking!', 'error');
        return;
    }
    
    if (confirm(`Bạn có chắc chắn muốn cập nhật trạng thái của ${bookingIds.length} booking thành ${status}?`)) {
        // Implement bulk update logic here
        console.log('Bulk update:', bookingIds, status);
    }
}

// Date range picker functionality
function initializeDateRangePicker() {
    const startDate = document.getElementById('startDate');
    const endDate = document.getElementById('endDate');
    
    if (startDate && endDate) {
        // Set max date for start date (today)
        const today = new Date().toISOString().split('T')[0];
        startDate.max = today;
        
        // Update end date min when start date changes
        startDate.addEventListener('change', function() {
            endDate.min = this.value;
            if (endDate.value && endDate.value < this.value) {
                endDate.value = this.value;
            }
        });
        
        // Update start date max when end date changes
        endDate.addEventListener('change', function() {
            startDate.max = this.value;
        });
    }
}

// Real-time statistics update
function updateStatistics() {
    fetch('/admin/bookings/statistics-data')
        .then(response => response.json())
        .then(data => {
            // Update statistics cards
            updateStatCard('pending', data.pendingBookings);
            updateStatCard('confirmed', data.confirmedBookings);
            updateStatCard('completed', data.completedBookings);
            updateStatCard('cancelled', data.cancelledBookings);
        })
        .catch(error => {
            console.error('Error updating statistics:', error);
        });
}

function updateStatCard(type, value) {
    const statCard = document.querySelector(`.stat-card .stat-icon.${type} + .stat-info p`);
    if (statCard) {
        statCard.textContent = value.toLocaleString();
    }
}

// Auto-refresh statistics every 30 seconds
setInterval(updateStatistics, 30000);

// Keyboard shortcuts
document.addEventListener('keydown', function(e) {
    // Ctrl/Cmd + F: Focus search
    if ((e.ctrlKey || e.metaKey) && e.key === 'f') {
        e.preventDefault();
        const searchInput = document.getElementById('search');
        if (searchInput) {
            searchInput.focus();
        }
    }
    
    // Ctrl/Cmd + E: Export
    if ((e.ctrlKey || e.metaKey) && e.key === 'e') {
        e.preventDefault();
        exportBookings();
    }
    
    // Ctrl/Cmd + P: Print
    if ((e.ctrlKey || e.metaKey) && e.key === 'p') {
        e.preventDefault();
        printBooking();
    }
});

// Tooltip initialization
function initializeTooltips() {
    const tooltipElements = document.querySelectorAll('[title]');
    tooltipElements.forEach(element => {
        element.addEventListener('mouseenter', function(e) {
            const tooltip = document.createElement('div');
            tooltip.className = 'custom-tooltip';
            tooltip.textContent = this.title;
            tooltip.style.cssText = `
                position: absolute;
                background: #333;
                color: white;
                padding: 5px 10px;
                border-radius: 4px;
                font-size: 12px;
                z-index: 1000;
                pointer-events: none;
            `;
            
            document.body.appendChild(tooltip);
            
            const rect = this.getBoundingClientRect();
            tooltip.style.left = rect.left + (rect.width / 2) - (tooltip.offsetWidth / 2) + 'px';
            tooltip.style.top = rect.top - tooltip.offsetHeight - 5 + 'px';
            
            this._tooltip = tooltip;
        });
        
        element.addEventListener('mouseleave', function() {
            if (this._tooltip) {
                this._tooltip.remove();
                this._tooltip = null;
            }
        });
    });
}
