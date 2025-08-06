package fsa.training.travelee.service;

import fsa.training.travelee.dto.BookingRequest;
import fsa.training.travelee.entity.*;
import fsa.training.travelee.repository.BookingRepository;
import fsa.training.travelee.repository.TourRepository;
import fsa.training.travelee.repository.TourScheduleRepository;
import fsa.training.travelee.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final TourRepository tourRepository;
    private final TourScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    @Override
    public Booking createBooking(BookingRequest request, Long userId) {
        // Lấy thông tin tour
        Tour tour = tourRepository.findById(request.getTourId())
                .orElseThrow(() -> new RuntimeException("Tour không tồn tại"));

        // Lấy thông tin schedule
        TourSchedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new RuntimeException("Lịch trình không tồn tại"));

        // Lấy thông tin user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        // Kiểm tra số chỗ còn lại
        int totalGuests = request.getAdultCount() + request.getChildCount() + request.getInfantCount();
        if (!checkAvailability(request.getScheduleId(), totalGuests)) {
            throw new RuntimeException("Không đủ chỗ cho số lượng khách hàng này");
        }

        // Tính toán tiền
        BigDecimal totalAmount = calculateTotalAmount(request.getTourId(), 
                request.getAdultCount(), request.getChildCount(), request.getInfantCount());
        BigDecimal depositAmount = calculateDepositAmount(totalAmount);
        BigDecimal remainingAmount = totalAmount.subtract(depositAmount);

        // Tạo booking
        Booking booking = Booking.builder()
                .tour(tour)
                .user(user)
                .schedule(schedule)
                .departureDate(request.getDepartureDate())
                .returnDate(request.getReturnDate())
                .adultCount(request.getAdultCount())
                .childCount(request.getChildCount())
                .infantCount(request.getInfantCount())
                .totalAmount(totalAmount)
                .depositAmount(depositAmount)
                .remainingAmount(remainingAmount)
                .status(BookingStatus.PENDING)
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .customerPhone(request.getCustomerPhone())
                .customerAddress(request.getCustomerAddress())
                .specialRequests(request.getSpecialRequests())
                .paymentMethod(request.getPaymentMethod())
                .build();

        // Cập nhật số chỗ còn lại
        schedule.setAvailableSlots(schedule.getAvailableSlots() - totalGuests);
        scheduleRepository.save(schedule);

        // Lưu booking
        Booking savedBooking = bookingRepository.save(booking);

        // Gửi email xác nhận
        sendBookingConfirmationEmail(savedBooking);

        return savedBooking;
    }

    @Override
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));
    }

    @Override
    public Page<Booking> getBookingsByUser(Long userId, Pageable pageable) {
        return bookingRepository.findByUserId(userId, pageable);
    }

    @Override
    public Page<Booking> getBookingsByTour(Long tourId, Pageable pageable) {
        return bookingRepository.findByTourId(tourId, pageable);
    }

    @Override
    public Page<Booking> getBookingsByStatus(BookingStatus status, Pageable pageable) {
        return bookingRepository.findByStatus(status, pageable);
    }

    @Override
    public Booking updateBookingStatus(Long bookingId, BookingStatus status) {
        Booking booking = getBookingById(bookingId);
        booking.setStatus(status);
        return bookingRepository.save(booking);
    }

    @Override
    public Booking cancelBooking(Long bookingId, String reason) {
        Booking booking = getBookingById(bookingId);
        
        // Kiểm tra xem có thể hủy không
        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new RuntimeException("Không thể hủy booking đã hoàn thành");
        }

        // Cập nhật trạng thái
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setSpecialRequests(booking.getSpecialRequests() + "\nLý do hủy: " + reason);

        // Hoàn trả số chỗ
        TourSchedule schedule = booking.getSchedule();
        int totalGuests = booking.getAdultCount() + booking.getChildCount() + booking.getInfantCount();
        schedule.setAvailableSlots(schedule.getAvailableSlots() + totalGuests);
        scheduleRepository.save(schedule);

        Booking savedBooking = bookingRepository.save(booking);

        // Gửi email hủy
        sendBookingCancellationEmail(savedBooking, reason);

        return savedBooking;
    }

    @Override
    public BigDecimal calculateTotalAmount(Long tourId, Integer adultCount, Integer childCount, Integer infantCount) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new RuntimeException("Tour không tồn tại"));

        BigDecimal adultTotal = tour.getAdultPrice().multiply(BigDecimal.valueOf(adultCount));
        BigDecimal childTotal = tour.getChildPrice().multiply(BigDecimal.valueOf(childCount));
        BigDecimal infantTotal = BigDecimal.ZERO; // Trẻ sơ sinh miễn phí

        return adultTotal.add(childTotal).add(infantTotal);
    }

    @Override
    public BigDecimal calculateDepositAmount(BigDecimal totalAmount) {
        // Đặt cọc 50%
        return totalAmount.multiply(BigDecimal.valueOf(0.5)).setScale(0, RoundingMode.HALF_UP);
    }

    @Override
    public boolean checkAvailability(Long scheduleId, Integer totalGuests) {
        TourSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Lịch trình không tồn tại"));

        return schedule.getAvailableSlots() >= totalGuests;
    }

    @Override
    public List<Object[]> getBookingStatistics() {
        // Có thể implement thống kê booking theo thời gian, status, etc.
        return null;
    }

    @Override
    public void sendBookingConfirmationEmail(Booking booking) {
        // TODO: Implement gửi email xác nhận
        System.out.println("Gửi email xác nhận booking cho: " + booking.getCustomerEmail());
    }

    @Override
    public void sendBookingCancellationEmail(Booking booking, String reason) {
        // TODO: Implement gửi email hủy
        System.out.println("Gửi email hủy booking cho: " + booking.getCustomerEmail() + " - Lý do: " + reason);
    }
} 