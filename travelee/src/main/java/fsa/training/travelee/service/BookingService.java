package fsa.training.travelee.service;

import fsa.training.travelee.dto.BookingRequest;
import fsa.training.travelee.entity.Booking;
import fsa.training.travelee.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface BookingService {
    
    // Tạo booking mới
    Booking createBooking(BookingRequest request, Long userId);
    
    // Lấy booking theo ID
    Booking getBookingById(Long id);
    
    // Lấy danh sách booking theo user
    Page<Booking> getBookingsByUser(Long userId, Pageable pageable);
    
    // Lấy danh sách booking theo tour
    Page<Booking> getBookingsByTour(Long tourId, Pageable pageable);
    
    // Lấy danh sách booking theo status
    Page<Booking> getBookingsByStatus(BookingStatus status, Pageable pageable);
    
    // Cập nhật trạng thái booking
    Booking updateBookingStatus(Long bookingId, BookingStatus status);
    
    // Hủy booking
    Booking cancelBooking(Long bookingId, String reason);
    
    // Tính tổng tiền booking
    BigDecimal calculateTotalAmount(Long tourId, Integer adultCount, Integer childCount, Integer infantCount);
    
    // Tính tiền đặt cọc (50% tổng tiền)
    BigDecimal calculateDepositAmount(BigDecimal totalAmount);
    
    // Kiểm tra số chỗ còn lại
    boolean checkAvailability(Long scheduleId, Integer totalGuests);
    
    // Lấy thống kê booking
    List<Object[]> getBookingStatistics();
    
    // Gửi email xác nhận booking
    void sendBookingConfirmationEmail(Booking booking);
    
    // Gửi email hủy booking
    void sendBookingCancellationEmail(Booking booking, String reason);
} 