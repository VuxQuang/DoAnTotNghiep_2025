document.addEventListener('DOMContentLoaded', function() {
    // Hiệu ứng hover cho các card giá trị cốt lõi (about-value-card)
    document.querySelectorAll('.about-value-card').forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-6px) scale(1.03)';
        });
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'none';
        });
    });

    // Hiệu ứng hover cho các thành viên team
    document.querySelectorAll('.team-member').forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-4px) scale(1.03)';
        });
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'none';
        });
    });

    // Tự động scroll lên đầu trang khi load (tránh giữ vị trí cũ khi quay lại)
    window.scrollTo({ top: 0, behavior: 'smooth' });
});