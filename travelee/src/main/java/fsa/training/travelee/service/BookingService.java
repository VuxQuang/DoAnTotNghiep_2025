package fsa.training.travelee.service;

import fsa.training.travelee.dto.booking.BookingRequestDto;
import fsa.training.travelee.entity.booking.Booking;
import fsa.training.travelee.entity.booking.BookingStatus;
import fsa.training.travelee.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface BookingService {
    
    // Tạo booking mới
    Booking createBooking(BookingRequestDto bookingRequest, User user);
    
    // Lấy booking theo ID
    Booking getBookingById(Long id);
    
    // Lấy booking theo booking code
    Booking getBookingByCode(String bookingCode);
    
    // Lấy tất cả booking của user
    List<Booking> getBookingsByUser(User user);
    
    // Lấy booking của user với phân trang
    Page<Booking> getBookingsByUser(User user, Pageable pageable);
    
    // Lấy booking theo status
    List<Booking> getBookingsByStatus(BookingStatus status);
    
    // Lấy booking theo tour
    List<Booking> getBookingsByTour(Long tourId);
    
    // Lấy booking theo schedule
    List<Booking> getBookingsBySchedule(Long scheduleId);
    
    // Cập nhật status của booking
    Booking updateBookingStatus(Long bookingId, BookingStatus status);
    
    // Hủy booking
    Booking cancelBooking(Long bookingId, String reason);
    
    // Tính tổng tiền booking
    BigDecimal calculateTotalAmount(Long tourId, Long scheduleId, int adultCount, int childCount);
    
    // Kiểm tra availability của schedule
    boolean isScheduleAvailable(Long scheduleId, int adultCount, int childCount);
    
    // Tạo booking code
    String generateBookingCode();
    
    // Gửi email xác nhận booking
    void sendBookingConfirmationEmail(Booking booking);
    
    // Gửi email hủy booking
    void sendBookingCancellationEmail(Booking booking);
} 