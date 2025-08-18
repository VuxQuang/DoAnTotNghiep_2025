package fsa.training.travelee.service;

import fsa.training.travelee.entity.booking.Booking;

public interface EmailService {
    
    /**
     * Gửi email xác nhận booking
     * @param booking thông tin booking
     */
    void sendBookingConfirmationEmail(Booking booking);
    
    /**
     * Gửi email hủy booking
     * @param booking thông tin booking
     */
    void sendBookingCancellationEmail(Booking booking);
    
    /**
     * Gửi email thông báo thay đổi trạng thái booking
     * @param booking thông tin booking
     * @param oldStatus trạng thái cũ
     * @param newStatus trạng thái mới
     */
    void sendBookingStatusChangeEmail(Booking booking, String oldStatus, String newStatus);

    /**
     * Gửi email cảm ơn khi tour hoàn thành
     * @param booking thông tin booking
     */
    void sendBookingCompletionEmail(Booking booking);
}
