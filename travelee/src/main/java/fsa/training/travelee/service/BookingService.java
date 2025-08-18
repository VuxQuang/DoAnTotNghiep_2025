package fsa.training.travelee.service;

import fsa.training.travelee.dto.booking.BookingRequestDto;
import fsa.training.travelee.entity.booking.Booking;
import fsa.training.travelee.entity.booking.BookingStatus;
import fsa.training.travelee.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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


    // Lấy booking theo tour
    List<Booking> getBookingsByTour(Long tourId);

    // Lấy booking theo schedule
    List<Booking> getBookingsBySchedule(Long scheduleId);

    // Lấy booking theo status
    List<Booking> getBookingsByStatus(BookingStatus status);

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



    // Lấy tất cả booking với phân trang (cho admin)
    Page<Booking> getAllBookingsWithPagination(Pageable pageable);

    // Lấy booking theo status với phân trang
    Page<Booking> getBookingsByStatusWithPagination(BookingStatus status, Pageable pageable);

    // Tìm kiếm booking (cho admin)
    Page<Booking> searchBookings(String searchTerm, Pageable pageable);

    // Lấy booking theo khoảng thời gian với phân trang
    Page<Booking> getBookingsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Thống kê tổng số booking
    long getTotalBookings();

    // Thống kê số booking theo status
    long getTotalBookingsByStatus(BookingStatus status);
}