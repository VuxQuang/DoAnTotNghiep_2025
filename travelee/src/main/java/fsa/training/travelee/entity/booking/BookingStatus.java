package fsa.training.travelee.entity.booking;

public enum BookingStatus {
    PENDING,        // Khách vừa đặt, chưa xác nhận, chưa thanh toán
    CONFIRMED,      // Admin xác nhận giữ chỗ, chưa thanh toán
    PAID,           // Đã thanh toán
    CANCELLED,      // Đã hủy
    COMPLETED,      // Hoàn thành tour
    REFUNDED        // Đã hoàn tiền
}
