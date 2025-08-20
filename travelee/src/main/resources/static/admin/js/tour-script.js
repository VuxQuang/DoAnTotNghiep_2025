
function confirmDelete(tourId, tourTitle) {
    return confirm(`Bạn có chắc chắn muốn xóa tour "${tourTitle}"?\n\nHành động này không thể hoàn tác!`);
}


document.addEventListener('DOMContentLoaded', function () {
    // Xử lý thông báo thành công
    const successMessage = document.getElementById('successMessage');
    if (successMessage) {
        alert(successMessage.textContent);
    }

    // Xử lý xác nhận xóa cho tất cả nút delete
    const deleteButtons = document.querySelectorAll('.action-btn.delete');
    deleteButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            const tourId = this.getAttribute('data-tour-id');
            const tourTitle = this.getAttribute('data-tour-title');
            
            if (!confirmDelete(tourId, tourTitle)) {
                e.preventDefault();
            }
        });
    });

    // Xử lý tìm kiếm tour
    const searchInput = document.getElementById('tour-search-input');
    if (searchInput) {
        searchInput.addEventListener('input', function() {
            const searchTerm = this.value.toLowerCase();
            const tourRows = document.querySelectorAll('tbody tr');
            
            tourRows.forEach(row => {
                const title = row.querySelector('td:nth-child(2)').textContent.toLowerCase();
                const category = row.querySelector('td:nth-child(4)').textContent.toLowerCase();
                
                if (title.includes(searchTerm) || category.includes(searchTerm)) {
                    row.style.display = '';
                } else {
                    row.style.display = 'none';
                }
            });
        });
    }
});
