// === Biến global ===
let uploadedImages = [];
let itineraryCounter = 0;
let scheduleCounter = 0;

const CLOUD_NAME = 'dodna125k';
const UPLOAD_PRESET = 'unsigned_preset';

// === Khi trang load ===
document.addEventListener('DOMContentLoaded', function () {
    initializeImageUpload();
    initializeForm();
    addItinerary();
    addSchedule();
});

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
    preview.innerHTML = '';

    uploadedImages.forEach((image, index) => {
        const item = document.createElement('div');
        item.className = 'image-preview-item';
        item.innerHTML = `
            <img src="${image.url}" alt="Ảnh ${index + 1}">
            <input type="hidden" name="imageUrls" value="${image.url}">
            <button type="button" class="remove-image" onclick="removeImage(${index})">×</button>
        `;
        preview.appendChild(item);
    });
}

// === Xoá ảnh đã upload (cả UI & mảng) ===
function removeImage(index) {
    uploadedImages.splice(index, 1);
    updateImagePreview();
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

    if (uploadedImages.length === 0) {
        alert('Vui lòng upload ít nhất 1 hình ảnh cho tour.');
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
            <div class="input-icon">
                <i class="fas fa-align-left"></i>
                <textarea name="itineraries[${index}].description" placeholder="Mô tả chi tiết hoạt động ngày ${index + 1}" rows="3"></textarea>
            </div>
        </div>
        <div class="form-group full-width">
            <label>Hoạt động cụ thể</label>
            <div class="input-icon">
                <i class="fas fa-list"></i>
                <textarea name="itineraries[${index}].activities" placeholder="Danh sách hoạt động" rows="2"></textarea>
            </div>
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
}

function removeSchedule(button) {
    button.closest('.schedule-item').remove();
}
