document.addEventListener('DOMContentLoaded', function() {
    // Lấy tất cả các nút trả lời và thêm sự kiện
    const replyButtons = document.querySelectorAll('.action-btn.reply');
    replyButtons.forEach(button => {
        button.addEventListener('click', function(event) {
            const supportRequestId = event.target.getAttribute('data-id');
            openReplyForm(supportRequestId);
        });
    });

    // Lấy tất cả các nút "Xem trả lời" và thêm sự kiện
    const viewButtons = document.querySelectorAll('.action-btn.view');
    viewButtons.forEach(button => {
        button.addEventListener('click', function(event) {
            const supportRequestId = event.target.getAttribute('data-id');
            viewAnswerSupport(supportRequestId);
        });
    });

});

// Mở form trả lời
function openReplyForm(supportRequestId) {
    // Tìm kiếm yêu cầu hỗ trợ trong danh sách yêu cầu
    const supportRequest = supports.find(req => req.id === supportRequestId);

    // Mở form trả lời và điền dữ liệu vào form
    document.getElementById('modal-username').value = supportRequest.user ? supportRequest.user.username : '';
    document.getElementById('modal-email').value = supportRequest.senderEmail;
    document.getElementById('modal-fullname').value = supportRequest.senderName;
    document.getElementById('modal-phone').value = supportRequest.senderPhone;
    document.getElementById('modal-title').value = supportRequest.title;
    document.getElementById('modal-content').value = supportRequest.content;
    document.getElementById('modal-reply').value = '';

    // Hiển thị form trả lời
    document.getElementById('replyFormContainer').style.display = 'block';
}

// Đóng form trả lời
function closeReplyForm() {
    document.getElementById('replyFormContainer').style.display = 'none';
}

// Xem nội dung trả lời
function viewAnswerSupport(supportRequestId) {
    const supportRequest = supports.find(req => req.id === supportRequestId);

    document.getElementById('viewAnswerContainer').innerHTML = `
        <div class="user-table-container">
            <div class="user-table-header">
                <div class="view-answer-title">Xem nội dung trả lời hỗ trợ</div>
            </div>
            <form onsubmit="return false;">
                <div class="form-row">
                    <div class="form-group">
                        <label>Tiêu đề:</label>
                        <input type="text" value="${supportRequest.title}" readonly>
                    </div>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label>Nội dung yêu cầu:</label>
                        <textarea rows="3" readonly>${supportRequest.content}</textarea>
                    </div>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label>Nội dung trả lời:</label>
                        <textarea rows="4" readonly>${supportRequest.reply || ''}</textarea>
                    </div>
                </div>
                <div class="form-actions">
                    <button type="button" class="action-btn delete" onclick="closeViewAnswer()">Đóng</button>
                </div>
            </form>
        </div>
    `;
    document.getElementById('viewAnswerContainer').style.display = 'block';
}

// Đóng form xem trả lời
function closeViewAnswer() {
    document.getElementById('viewAnswerContainer').style.display = 'none';
}
