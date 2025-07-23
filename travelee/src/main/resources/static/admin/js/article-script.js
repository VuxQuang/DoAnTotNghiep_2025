document.addEventListener("DOMContentLoaded", function () {
    const contentTextarea = document.getElementById("content");
    if (!contentTextarea) {
        console.error("Content textarea not found");
        return;
    }

    ClassicEditor
        .create(contentTextarea, {
            extraPlugins: [ MyCustomUploadAdapterPlugin ],
            toolbar: [
                'heading', '|',
                'bold', 'italic', '|',
                'link', '|',
                'bulletedList', 'numberedList', '|',
                'blockQuote', '|',
                'insertTable', '|',
                'imageUpload', '|',
                'imageInsert', '|',
                'undo', 'redo'
            ],
            placeholder: 'Nhập nội dung bài viết tại đây...'
        })
        .then(editor => {
            window.editorInstance = editor;
            console.log("CKEditor initialized successfully");
        })
        .catch(error => {
            console.error("CKEditor load error:", error);
        });

    // Initialize create article form
    initCreateArticleForm();
});

// Global variables for tag selection
let selectedCategories = new Set();

// Preview ảnh bìa
function previewImage(event) {
    const input = event.target;
    const preview = document.getElementById('thumbnailPreview');
    const label = document.querySelector('.file-upload-label span');
    
    if (input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = function(e) {
            preview.src = e.target.result;
            preview.style.display = 'block';
            label.textContent = input.files[0].name;
        };
        reader.readAsDataURL(input.files[0]);
    } else {
        preview.src = '';
        preview.style.display = 'none';
        label.textContent = 'Chọn ảnh bìa';
    }
}

// Add tag to selected categories
function addTag(id, name) {
    const tag = document.createElement('div');
    tag.className = 'tag selected';
    tag.innerHTML = `
        <span>${name}</span>
        <button type="button" class="tag-remove" onclick="removeTag(${id}, this)">
            <i class="fas fa-times"></i>
        </button>
    `;
    document.getElementById('selectedTags').appendChild(tag);
}

// Remove tag from selected categories
function removeTag(id, button) {
    selectedCategories.delete(id);
    button.parentElement.remove();
    updateHiddenInput();
}

// Update hidden input fields for form submission
function updateHiddenInput() {
    // Remove existing hidden inputs
    const existingInputs = document.querySelectorAll('input[name="categories"]');
    existingInputs.forEach(input => input.remove());
    
    // Add new hidden inputs for selected categories
    selectedCategories.forEach(categoryId => {
        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = 'categories';
        input.value = categoryId;
        document.getElementById('createArticleForm').appendChild(input);
    });
}

// Initialize create article form functionality
function initCreateArticleForm() {
    const categorySelect = document.getElementById('categorySelect');
    const selectedTagsContainer = document.getElementById('selectedTags');
    
    if (!categorySelect || !selectedTagsContainer) {
        return; // Not on create article page
    }
    
    // Load existing categories if editing
    const existingCategories = window.existingCategories || [];
    if (existingCategories && existingCategories.length > 0) {
        existingCategories.forEach(cat => {
            selectedCategories.add(cat.id);
            addTag(cat.id, cat.name);
        });
    }
    
    // Load existing thumbnail
    const oldThumb = window.existingThumbnail || '';
    if (oldThumb && oldThumb !== '') {
        const preview = document.getElementById('thumbnailPreview');
        if (preview) {
            preview.src = oldThumb;
            preview.style.display = 'block';
            const label = document.querySelector('.file-upload-label span');
            if (label) {
                label.textContent = 'Ảnh đã chọn';
            }
        }
    }
    
    // Handle category selection
    categorySelect.addEventListener('change', function() {
        const selectedOption = this.options[this.selectedIndex];
        if (selectedOption.value && !selectedCategories.has(parseInt(selectedOption.value))) {
            selectedCategories.add(parseInt(selectedOption.value));
            addTag(selectedOption.value, selectedOption.text);
            updateHiddenInput();
        }
        this.value = '';
    });
    
    // Simple form submission without validation
    const form = document.getElementById('createArticleForm');
    if (form) {
        form.addEventListener('submit', function(e) {
            console.log('Form submitted, updating content...');
            
            // Update content from CKEditor
            if (window.editorInstance) {
                const editorData = window.editorInstance.getData();
                const contentTextarea = document.getElementById('content');
                if (contentTextarea) {
                    contentTextarea.value = editorData;
                }
                console.log('Content length:', editorData.length);
                console.log('Content preview:', editorData.substring(0, 100) + '...');
            }
            
            console.log('Form submitted, updating categories...');
            updateHiddenInput(); // Ensure categories are updated
        });
    }
}

// Export functions for global access
window.previewImage = previewImage;
window.addTag = addTag;
window.removeTag = removeTag;
window.updateHiddenInput = updateHiddenInput;
