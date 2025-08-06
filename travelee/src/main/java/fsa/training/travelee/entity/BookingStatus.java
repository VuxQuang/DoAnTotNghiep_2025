package fsa.training.travelee.entity;

public enum BookingStatus {
    PENDING("Chờ xác nhận"),
    CONFIRMED("Đã xác nhận"),
    PAID("Đã thanh toán"),
    CANCELLED("Đã hủy"),
    COMPLETED("Hoàn thành");

    private final String displayName;

    BookingStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 