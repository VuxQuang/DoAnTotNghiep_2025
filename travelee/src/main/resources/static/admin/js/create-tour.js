// === Biến global ===
let uploadedImages = [];
let itineraryCounter = 0;
let scheduleCounter = 0;
let includesCounter = 0;
let excludesCounter = 0;

const CLOUD_NAME = 'dodna125k';
const UPLOAD_PRESET = 'unsigned_preset';

// === Khi trang load ===
document.addEventListener('DOMContentLoaded', function () {
    const isReadOnly = document.body.getAttribute('data-readonly') === 'true';

    if (isReadOnly) {
        enableReadonlyMode();
        return; // Không khởi tạo các chức năng thêm/sửa
    }

    initializeImageUpload();
    initializeForm();
    initializeHashtagSelection();
    initializeDateCalculation();
    
    // Chỉ thêm itinerary và schedule mới khi tạo tour mới
    // Khi xem tour, dữ liệu sẽ được load từ template
    if (!isReadOnly) {
        addItinerary();
        addSchedule();
    } else {
        // Cập nhật counter để tránh xung đột index khi thêm mới
        const existingItineraries = document.querySelectorAll('#itineraryContainer .itinerary-item');
        const existingSchedules = document.querySelectorAll('#scheduleContainer .schedule-item');
        const existingIncludes = document.querySelectorAll('#includesContainer .input-group');
        const existingExcludes = document.querySelectorAll('#excludesContainer .input-group');
        
        itineraryCounter = existingItineraries.length;
        scheduleCounter = existingSchedules.length;
        includesCounter = Math.max(existingIncludes.length - 1, 0); // Trừ đi input mới
        excludesCounter = Math.max(existingExcludes.length - 1, 0); // Trừ đi input mới
        
        // Kiểm tra và cập nhật validation ảnh
        updateImageValidation();
    }
});

function enableReadonlyMode() {
    const form = document.getElementById('createTourForm');
    if (form) {
        form.addEventListener('submit', function(e) { e.preventDefault(); });
    }

    // Vô hiệu hoá upload ảnh
    const uploadZone = document.getElementById('uploadZone');
    if (uploadZone) {
        uploadZone.style.pointerEvents = 'none';
    }

    // Vô hiệu hoá toàn bộ input interactions (đã có readonly/disabled từ template)
    const allInputs = document.querySelectorAll('input, textarea, select');
    allInputs.forEach(input => {
        input.style.pointerEvents = 'none';
    });

    // Ghi đè các hàm động để tránh thao tác
    window.addInput = function() { return false; };
    window.removeInput = function() { return false; };
    window.addItinerary = function() { return false; };
    window.addSchedule = function() { return false; };
    window.removeItinerary = function() { return false; };
    window.removeSchedule = function() { return false; };
}

// === Khởi tạo upload ảnh Cloudinary ===
function initializeImageUpload() {
    const uploadZone = document.getElementById('uploadZone');
    const imageInput = document.getElementById('imageInput');

    uploadZone.addEventListener('click', () => imageInput.click());
    imageInput.addEventListener('change', (e) => handleFiles(e.target.files));

    uploadZone.addEventListener('dragover', (e) => {
        e.preventDefault();
        uploadZone.style.background = '#e8f0ff';
    });

    uploadZone.addEventListener('dragleave', (e) => {
        e.preventDefault();
        uploadZone.style.background = '#f8f9ff';
    });

    uploadZone.addEventListener('drop', (e) => {
        e.preventDefault();
        uploadZone.style.background = '#f8f9ff';
        handleFiles(e.dataTransfer.files);
    });
}

// === Upload từng file lên Cloudinary ===
function handleFiles(files) {
    Array.from(files).forEach(file => {
        if (!file.type.startsWith('image/')) return;

        const data = new FormData();
        data.append('file', file);
        data.append('upload_preset', UPLOAD_PRESET);

        fetch(`https://api.cloudinary.com/v1_1/${CLOUD_NAME}/image/upload`, {
            method: 'POST',
            body: data
        })
            .then(res => res.json())
            .then(result => {
                uploadedImages.push({
                    url: result.secure_url,
                    isPrimary: uploadedImages.length === 0,
                    sortOrder: uploadedImages.length
                });
                updateImagePreview();
            })
            .catch(err => {
                alert("Lỗi khi upload ảnh lên Cloudinary");
                console.error(err);
            });
    });
}

// === Hiển thị ảnh + input hidden để submit ===
function updateImagePreview() {
    const preview = document.getElementById('imagePreview');
    
    // Giữ lại các ảnh cũ (nếu có) - không xóa
    // Chỉ xóa những ảnh mới upload để cập nhật
    const newImages = preview.querySelectorAll('.image-preview-item[data-new]');
    newImages.forEach(img => img.remove());

    // Thêm các ảnh mới upload
    uploadedImages.forEach((image, index) => {
        const item = document.createElement('div');
        item.className = 'image-preview-item';
        item.setAttribute('data-new', 'true');
        item.innerHTML = `
            <img src="${image.url}" alt="Ảnh mới ${index + 1}">
            <input type="hidden" name="imageUrls" value="${image.url}">
            <button type="button" class="remove-image" onclick="removeImage(${index})" title="Xóa ảnh mới">×</button>
        `;
        preview.appendChild(item);
    });
    
    // Cập nhật validation sau khi thay đổi ảnh
    updateImageValidation();
}

// === Xoá ảnh đã upload (cả UI & mảng) ===
function removeImage(index) {
    if (confirm('Bạn có chắc muốn xóa ảnh mới này?')) {
        uploadedImages.splice(index, 1);
        updateImagePreview();
    }
}

// === Xóa ảnh cũ ===
function removeExistingImage(element) {
    if (confirm('Bạn có chắc muốn xóa ảnh cũ này? Ảnh sẽ bị xóa khỏi tour.')) {
        const imageItem = element.closest('.image-preview-item');
        imageItem.remove();
        
        // Cập nhật validation nếu cần
        updateImageValidation();
    }
}

// === Cập nhật validation ảnh ===
function updateImageValidation() {
    const existingImages = document.querySelectorAll('#imagePreview .image-preview-item img');
    const totalImages = uploadedImages.length + existingImages.length;
    
    // Hiển thị thông báo nếu không còn ảnh nào
    if (totalImages === 0) {
        const preview = document.getElementById('imagePreview');
        if (!preview.querySelector('.no-images-message')) {
            const message = document.createElement('div');
            message.className = 'no-images-message';
            message.innerHTML = '<p style="color: #ff6b6b; text-align: center; padding: 20px;">⚠️ Chưa có ảnh nào cho tour. Vui lòng upload ít nhất 1 ảnh.</p>';
            preview.appendChild(message);
        }
    } else {
        // Xóa thông báo nếu có ảnh
        const message = document.querySelector('.no-images-message');
        if (message) message.remove();
    }
    
    // Cập nhật thống kê ảnh
    updateImageStats();
}

// === Cập nhật thống kê ảnh ===
function updateImageStats() {
    const existingImages = document.querySelectorAll('#imagePreview .image-preview-item img');
    const totalImages = uploadedImages.length + existingImages.length;
    
    // Tìm hoặc tạo element hiển thị thống kê
    let statsElement = document.querySelector('.image-stats');
    if (!statsElement) {
        statsElement = document.createElement('div');
        statsElement.className = 'image-stats';
        statsElement.style.cssText = 'margin-top: 10px; padding: 8px; background: #f8f9fa; border-radius: 6px; font-size: 0.9rem; color: #666; text-align: center;';
        
        const preview = document.getElementById('imagePreview');
        preview.parentNode.insertBefore(statsElement, preview.nextSibling);
    }
    
    if (totalImages > 0) {
        statsElement.innerHTML = `
            📊 Tổng số ảnh: <strong>${totalImages}</strong> 
            (${existingImages.length} ảnh cũ, ${uploadedImages.length} ảnh mới)
        `;
    } else {
        statsElement.innerHTML = '📊 Chưa có ảnh nào';
    }
}

// === Form truyền thống (không cần fetch) ===
function initializeForm() {
    const form = document.getElementById('createTourForm');
    form.addEventListener('submit', function (e) {
        if (!validateForm()) {
            e.preventDefault();
        }
    });
}

// === Kiểm tra form hợp lệ ===
function validateForm() {
    const requiredFields = ['title', 'category', 'departure', 'destination', 'duration', 'maxParticipants', 'adultPrice', 'childPrice', 'status'];

    for (let fieldId of requiredFields) {
        const field = document.getElementById(fieldId);
        if (!field || !field.value.trim()) {
            alert(`Vui lòng điền đầy đủ trường: ${field.previousElementSibling.textContent.replace('*', '')}`);
            field.focus();
            return false;
        }
    }

    // Kiểm tra ảnh: cần có ít nhất 1 ảnh (từ upload mới hoặc ảnh cũ)
    const existingImages = document.querySelectorAll('#imagePreview .image-preview-item img');
    const totalImages = uploadedImages.length + existingImages.length;
    
    if (totalImages === 0) {
        alert('Vui lòng upload ít nhất 1 hình ảnh cho tour hoặc giữ lại ảnh cũ.');
        return false;
    }

    return true;
}

// === Thêm lịch trình ngày ===
function addItinerary() {
    const index = itineraryCounter++; // dùng index bắt đầu từ 0
    const container = document.getElementById('itineraryContainer');
    const item = document.createElement('div');
    item.className = 'itinerary-item';
    item.innerHTML = `
        <h4>
            Ngày ${index + 1}
            <button type="button" class="remove-item" onclick="removeItinerary(this)">×</button>
        </h4>
        <input type="hidden" name="itineraries[${index}].dayNumber" value="${index + 1}">
        <div class="form-row">
            <div class="form-group">
                <label>Tiêu đề ngày</label>
                <div class="input-icon">
                    <i class="fas fa-heading"></i>
                    <input type="text" name="itineraries[${index}].title" placeholder="Tiêu đề ngày ${index + 1}" required>
                </div>
            </div>
            <div class="form-group">
                <label>Bữa ăn</label>
                <div class="input-icon">
                    <i class="fas fa-utensils"></i>
                    <input type="text" name="itineraries[${index}].meals" placeholder="Sáng, trưa, tối">
                </div>
            </div>
        </div>
        <div class="form-group full-width">
            <label>Mô tả hoạt động</label>
            <div class="dynamic-inputs" id="descriptionContainer${index}">
                <div class="input-group">
                    <div class="input-icon">
                        <i class="fas fa-align-left"></i>
                        <input type="text" name="itineraries[${index}].description[0]" placeholder="Nhập mô tả hoạt động" class="dynamic-input">
                    </div>
                    <button type="button" class="btn-remove" onclick="removeInput(this)" style="display: none;">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </div>
            <button type="button" class="btn-add" onclick="addInput('descriptionContainer${index}', 'itineraries[${index}].description')">
                <i class="fas fa-plus"></i> Thêm mô tả
            </button>
        </div>
        <div class="form-group full-width">
            <label>Hoạt động cụ thể</label>
            <div class="dynamic-inputs" id="activitiesContainer${index}">
                <div class="input-group">
                    <div class="input-icon">
                        <i class="fas fa-list"></i>
                        <input type="text" name="itineraries[${index}].activities[0]" placeholder="Nhập hoạt động cụ thể" class="dynamic-input">
                    </div>
                    <button type="button" class="btn-remove" onclick="removeInput(this)" style="display: none;">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </div>
            <button type="button" class="btn-add" onclick="addInput('activitiesContainer${index}', 'itineraries[${index}].activities')">
                <i class="fas fa-plus"></i> Thêm hoạt động
            </button>
        </div>
        <div class="form-group full-width">
            <label>Nơi lưu trú</label>
            <div class="input-icon">
                <i class="fas fa-bed"></i>
                <input type="text" name="itineraries[${index}].accommodation" placeholder="Tên khách sạn/nơi lưu trú">
            </div>
        </div>
    `;
    container.appendChild(item);
}

function removeItinerary(button) {
    button.closest('.itinerary-item').remove();
}

// === Thêm lịch khởi hành ===
function addSchedule() {
    const index = scheduleCounter++;
    const container = document.getElementById('scheduleContainer');
    const item = document.createElement('div');
    item.className = 'schedule-item';
    item.innerHTML = `
        <h4>
            Lịch khởi hành ${index + 1}
            <button type="button" class="remove-item" onclick="removeSchedule(this)">×</button>
        </h4>
        <div class="form-row">
            <div class="form-group">
                <label>Ngày khởi hành</label>
                <div class="input-icon">
                    <i class="fas fa-calendar"></i>
                    <input type="date" name="schedules[${index}].departureDate" required>
                </div>
            </div>
            <div class="form-group">
                <label>Ngày về</label>
                <div class="input-icon">
                    <i class="fas fa-calendar-check"></i>
                    <input type="date" name="schedules[${index}].returnDate" required>
                </div>
            </div>
        </div>
        <div class="form-row">
            <div class="form-group">
                <label>Giá đặc biệt (VNĐ)</label>
                <div class="input-icon">
                    <i class="fas fa-dollar-sign"></i>
                    <input type="number" name="schedules[${index}].specialPrice" placeholder="Giá khuyến mãi (nếu có)" min="0">
                </div>
            </div>
            <div class="form-group">
                <label>Số chỗ còn lại</label>
                <div class="input-icon">
                    <i class="fas fa-users"></i>
                    <input type="number" name="schedules[${index}].availableSlots" placeholder="Số chỗ" min="0" required>
                </div>
            </div>
        </div>
        <div class="form-group">
            <label>Trạng thái</label>
            <div class="input-icon">
                <i class="fas fa-toggle-on"></i>
                <select name="schedules[${index}].status" required>
                    <option value="available">Còn chỗ</option>
                    <option value="limited">Sắp hết</option>
                    <option value="full">Đã đầy</option>
                    <option value="closed">Đã đóng</option>
                </select>
            </div>
        </div>
    `;
    container.appendChild(item);
    
    // Auto-calculate return date for new schedule
    const newScheduleItem = container.lastElementChild;
    updateReturnDateForSchedule(newScheduleItem);
    
    // Add event listener for departure date change
    const departureDateInput = newScheduleItem.querySelector('input[name*="departureDate"]');
    if (departureDateInput) {
        setMinimumDateForInput(departureDateInput);
        departureDateInput.addEventListener('change', function() {
            updateReturnDateForSchedule(newScheduleItem);
        });
    }
}

function removeSchedule(button) {
    button.closest('.schedule-item').remove();
}

// === Functions cho includes/excludes ===
function addInput(containerId, fieldName) {
    const container = document.getElementById(containerId);
    const nextIndex = container.querySelectorAll('input.dynamic-input').length; // tính index kế tiếp trong container

    const inputGroup = document.createElement('div');
    inputGroup.className = 'input-group';
    const isIncludeExclude = fieldName === 'includes' || fieldName === 'excludes';
    const icon = fieldName.includes('activities') ? 'list' : (fieldName.includes('description') ? 'align-left' : (fieldName === 'includes' ? 'plus' : 'minus'));
    const nameWithIndex = isIncludeExclude ? `${fieldName}[${nextIndex}]` : `${fieldName}[${nextIndex}]`;

    inputGroup.innerHTML = `
        <div class="input-icon">
            <i class="fas fa-${icon}"></i>
            <input type="text" name="${nameWithIndex}" placeholder="Nhập ${isIncludeExclude ? (fieldName === 'includes' ? 'dịch vụ bao gồm' : 'dịch vụ không bao gồm') : 'nội dung'}" class="dynamic-input">
        </div>
        <button type="button" class="btn-remove" onclick="removeInput(this)">
            <i class="fas fa-trash"></i>
        </button>
    `;

    container.appendChild(inputGroup);

    // Hiển thị nút remove cho tất cả input groups
    const allInputGroups = container.querySelectorAll('.input-group');
    allInputGroups.forEach(group => {
        const removeBtn = group.querySelector('.btn-remove');
        removeBtn.style.display = allInputGroups.length > 1 ? 'block' : 'none';
    });
}

function removeInput(button) {
    const inputGroup = button.closest('.input-group');
    const container = inputGroup.parentElement;
    
    inputGroup.remove();
    
    // Ẩn nút remove nếu chỉ còn 1 input
    const remainingInputs = container.querySelectorAll('.input-group');
    if (remainingInputs.length === 1) {
        const removeBtn = remainingInputs[0].querySelector('.btn-remove');
        removeBtn.style.display = 'none';
    }
}

// === Hashtag Category Selection ===
function initializeHashtagSelection() {
    const hashtagInput = document.querySelector('.hashtag-input');
    const hashtagDropdown = document.querySelector('.hashtag-dropdown');
    const hashtagOptions = document.querySelectorAll('.hashtag-option');
    const selectedTagsContainer = document.getElementById('selectedTags');
    const hiddenCheckboxes = document.querySelectorAll('.hidden-inputs input[type="checkbox"]');
    
    let selectedCategories = new Set();
    
    // Load existing selections from hidden checkboxes
    hiddenCheckboxes.forEach(checkbox => {
        if (checkbox.checked) {
            const categoryId = checkbox.value;
            const categoryName = checkbox.closest('.hashtag-option')?.dataset.categoryName || 
                               document.querySelector(`[data-category-id="${categoryId}"]`)?.dataset.categoryName;
            if (categoryName) {
                selectedCategories.add(categoryId);
                updateSelectedTags();
            }
        }
    });
    
    // Show dropdown when input is clicked
    hashtagInput.addEventListener('click', function() {
        if (!hashtagInput.disabled) {
            hashtagDropdown.classList.toggle('show');
            updateDropdownOptions();
        }
    });
    
    // Hide dropdown when clicking outside
    document.addEventListener('click', function(e) {
        if (!e.target.closest('.hashtag-container')) {
            hashtagDropdown.classList.remove('show');
        }
    });
    
    // Handle option selection
    hashtagOptions.forEach(option => {
        option.addEventListener('click', function() {
            const categoryId = this.dataset.categoryId;
            const categoryName = this.dataset.categoryName;
            
            if (selectedCategories.has(categoryId)) {
                // Remove selection
                selectedCategories.delete(categoryId);
                this.classList.remove('selected');
                this.querySelector('i').className = 'fas fa-plus';
            } else {
                // Add selection
                selectedCategories.add(categoryId);
                this.classList.add('selected');
                this.querySelector('i').className = 'fas fa-check';
            }
            
            updateSelectedTags();
            updateHiddenCheckboxes();
            hashtagDropdown.classList.remove('show');
        });
    });
    
    function updateSelectedTags() {
        selectedTagsContainer.innerHTML = '';
        
        selectedCategories.forEach(categoryId => {
            const categoryName = document.querySelector(`[data-category-id="${categoryId}"]`)?.dataset.categoryName;
            if (categoryName) {
                const tag = document.createElement('div');
                tag.className = 'hashtag-tag';
                tag.innerHTML = `
                    <span>${categoryName}</span>
                    <span class="remove-tag" data-category-id="${categoryId}">×</span>
                `;
                
                // Add remove functionality
                tag.querySelector('.remove-tag').addEventListener('click', function(e) {
                    e.stopPropagation();
                    const idToRemove = this.dataset.categoryId;
                    selectedCategories.delete(idToRemove);
                    
                    // Update option appearance
                    const option = document.querySelector(`[data-category-id="${idToRemove}"]`);
                    if (option) {
                        option.classList.remove('selected');
                        option.querySelector('i').className = 'fas fa-plus';
                    }
                    
                    updateSelectedTags();
                    updateHiddenCheckboxes();
                });
                
                selectedTagsContainer.appendChild(tag);
            }
        });
    }
    
    function updateHiddenCheckboxes() {
        hiddenCheckboxes.forEach(checkbox => {
            checkbox.checked = selectedCategories.has(checkbox.value);
        });
    }
    
    function updateDropdownOptions() {
        hashtagOptions.forEach(option => {
            const categoryId = option.dataset.categoryId;
            if (selectedCategories.has(categoryId)) {
                option.classList.add('selected');
                option.querySelector('i').className = 'fas fa-check';
            } else {
                option.classList.remove('selected');
                option.querySelector('i').className = 'fas fa-plus';
            }
        });
    }
    
    // Initialize
    updateSelectedTags();
    updateHiddenCheckboxes();
}

// === Auto Calculate Return Date ===
function initializeDateCalculation() {
    const durationInput = document.getElementById('duration');
    const scheduleContainer = document.getElementById('scheduleContainer');
    
    if (!durationInput || !scheduleContainer) return;
    
    // Set minimum date for all existing departure date inputs
    setMinimumDateForAllSchedules();
    
    // Listen for duration changes
    durationInput.addEventListener('input', function() {
        updateAllReturnDates();
    });
    
    // Listen for departure date changes in existing schedules
    scheduleContainer.addEventListener('change', function(e) {
        if (e.target.name && e.target.name.includes('departureDate')) {
            const scheduleItem = e.target.closest('.schedule-item');
            updateReturnDateForSchedule(scheduleItem);
        }
    });
    
    // Add event listeners to existing schedules (for edit mode)
    const existingSchedules = scheduleContainer.querySelectorAll('.schedule-item');
    existingSchedules.forEach(scheduleItem => {
        const departureDateInput = scheduleItem.querySelector('input[name*="departureDate"]');
        if (departureDateInput) {
            setMinimumDateForInput(departureDateInput);
            departureDateInput.addEventListener('change', function() {
                updateReturnDateForSchedule(scheduleItem);
            });
        }
    });
}

function updateAllReturnDates() {
    const duration = parseInt(document.getElementById('duration').value);
    if (!duration || duration <= 0) return;
    
    const scheduleItems = document.querySelectorAll('#scheduleContainer .schedule-item');
    scheduleItems.forEach(item => {
        updateReturnDateForSchedule(item, duration);
    });
}

function updateReturnDateForSchedule(scheduleItem, duration = null) {
    if (!duration) {
        duration = parseInt(document.getElementById('duration').value);
    }
    
    if (!duration || duration <= 0) return;
    
    const departureDateInput = scheduleItem.querySelector('input[name*="departureDate"]');
    const returnDateInput = scheduleItem.querySelector('input[name*="returnDate"]');
    
    if (!departureDateInput || !returnDateInput) return;
    
    const departureDate = departureDateInput.value;
    if (!departureDate) return;
    
    // Calculate return date (departure + duration days)
    const departure = new Date(departureDate);
    const returnDate = new Date(departure);
    returnDate.setDate(departure.getDate() + duration);
    
    // Format date for input (YYYY-MM-DD)
    const returnDateString = returnDate.toISOString().split('T')[0];
    returnDateInput.value = returnDateString;
    
    // Add visual feedback
    returnDateInput.style.backgroundColor = '#e8f5e8';
    returnDateInput.style.borderColor = '#28a745';
    
    // Remove visual feedback after 2 seconds
    setTimeout(() => {
        returnDateInput.style.backgroundColor = '';
        returnDateInput.style.borderColor = '';
    }, 2000);
}

// === Date Validation Functions ===
function setMinimumDateForAllSchedules() {
    const departureDateInputs = document.querySelectorAll('input[name*="departureDate"]');
    departureDateInputs.forEach(input => {
        setMinimumDateForInput(input);
    });
}

function setMinimumDateForInput(input) {
    if (!input) return;
    
    // Set minimum date to today
    const today = new Date();
    const todayString = today.toISOString().split('T')[0];
    input.setAttribute('min', todayString);
    
    // Add validation message if needed
    input.addEventListener('invalid', function() {
        if (this.validity.rangeUnderflow) {
            this.setCustomValidity('Ngày khởi hành không được là ngày quá khứ. Vui lòng chọn ngày hôm nay hoặc ngày sau.');
        } else {
            this.setCustomValidity('');
        }
    });
    
    // Clear custom validity when user starts typing
    input.addEventListener('input', function() {
        this.setCustomValidity('');
    });
}
