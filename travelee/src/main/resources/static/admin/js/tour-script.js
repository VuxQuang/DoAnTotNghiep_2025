
function confirmDelete() {
    return confirm("Bạn có chắc chắn muốn xoá tour này?");
}


document.addEventListener('DOMContentLoaded', function () {
    const successMessage = document.getElementById('successMessage');
    if (successMessage) {
        alert(successMessage.textContent);
    }


});
