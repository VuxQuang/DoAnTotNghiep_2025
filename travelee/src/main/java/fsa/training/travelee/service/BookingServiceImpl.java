package fsa.training.travelee.service;

import fsa.training.travelee.dto.booking.BookingRequestDto;
import fsa.training.travelee.entity.booking.Booking;
import fsa.training.travelee.entity.booking.BookingParticipant;
import fsa.training.travelee.entity.booking.BookingStatus;
import fsa.training.travelee.entity.booking.ParticipantType;
import fsa.training.travelee.entity.payment.Payment;
import fsa.training.travelee.entity.payment.PaymentMethod;
import fsa.training.travelee.entity.payment.PaymentStatus;
import fsa.training.travelee.entity.Tour;
import fsa.training.travelee.entity.TourSchedule;
import fsa.training.travelee.entity.User;
import fsa.training.travelee.repository.BookingRepository;
import fsa.training.travelee.repository.TourRepository;
import fsa.training.travelee.repository.TourScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final TourRepository tourRepository;
    private final TourScheduleRepository tourScheduleRepository;

    @Override
    public Booking createBooking(BookingRequestDto bookingRequest, User user) {
        // Kiểm tra tour và schedule
        Tour tour = tourRepository.findById(bookingRequest.getTourId())
                .orElseThrow(() -> new IllegalArgumentException("Tour không tồn tại"));
        
        TourSchedule schedule = tourScheduleRepository.findById(bookingRequest.getScheduleId())
                .orElseThrow(() -> new IllegalArgumentException("Lịch trình không tồn tại"));
        
        // Kiểm tra availability
        if (!isScheduleAvailable(bookingRequest.getScheduleId(), 
                                bookingRequest.getAdultCount(), 
                                bookingRequest.getChildCount())) {
            throw new IllegalArgumentException("Lịch trình không đủ chỗ");
        }
        
        // Tính tổng tiền
        BigDecimal totalAmount = calculateTotalAmount(
                bookingRequest.getTourId(),
                bookingRequest.getScheduleId(),
                bookingRequest.getAdultCount(),
                bookingRequest.getChildCount()
        );
        
        // Tạo booking
        Booking booking = Booking.builder()
                .bookingCode(generateBookingCode())
                .adultCount(bookingRequest.getAdultCount())
                .childCount(bookingRequest.getChildCount())
                .totalAmount(totalAmount)
                .specialRequests(bookingRequest.getSpecialRequests())
                .status(BookingStatus.PENDING)
                .user(user)
                .tour(tour)
                .schedule(schedule)
                .build();
        
        // Lưu booking trước
        final Booking savedBooking = bookingRepository.save(booking);
        
        // Tạo participants với savedBooking (final)
        List<BookingParticipant> participants = new ArrayList<>();
        bookingRequest.getParticipants().forEach(participantRequest -> {
            BookingParticipant participant = BookingParticipant.builder()
                    .fullName(participantRequest.getFullName())
                    .dateOfBirth(LocalDate.parse(participantRequest.getDateOfBirth()))
                    .gender(participantRequest.getGender())
                    .idCard(participantRequest.getIdCard())
                    .phoneNumber(participantRequest.getPhoneNumber())
                    .type(ParticipantType.valueOf(participantRequest.getType()))
                    .booking(savedBooking)
                    .build();
            participants.add(participant);
        });
        
        Payment payment = Payment.builder()
                .paymentCode(generatePaymentCode())
                .amount(totalAmount)
                .status(PaymentStatus.PENDING)
                .booking(savedBooking)
                .build();
        
        savedBooking.setPayment(payment);
        savedBooking.setParticipants(participants);

        // Lưu booking (sẽ cascade persist cả participants và payment)
        Booking finalBooking = bookingRepository.save(savedBooking);
        
        // Gửi email xác nhận
        try {
            sendBookingConfirmationEmail(finalBooking);
        } catch (Exception e) {
            log.error("Lỗi gửi email xác nhận booking: {}", e.getMessage());
        }
        
        return finalBooking;
    }

    @Override
    public Booking getBookingById(Long id) {
        // Lấy booking với relationships cơ bản
        Booking booking = bookingRepository.findByIdWithRelationships(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking không tồn tại"));
        
        // Lấy thêm participants và payment
        Booking bookingWithDetails = bookingRepository.findByIdWithParticipantsAndPayment(id)
                .orElse(booking);
        
        // Merge thông tin
        if (bookingWithDetails != null) {
            booking.setParticipants(bookingWithDetails.getParticipants());
            booking.setPayment(bookingWithDetails.getPayment());
        }
        
        return booking;
    }

    @Override
    public Booking getBookingByCode(String bookingCode) {
        return bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new IllegalArgumentException("Booking code không tồn tại"));
    }

    @Override
    public List<Booking> getBookingsByUser(User user) {
        return bookingRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public Page<Booking> getBookingsByUser(User user, Pageable pageable) {
        return bookingRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    @Override
    public List<Booking> getBookingsByStatus(BookingStatus status) {
        return bookingRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    @Override
    public List<Booking> getBookingsByTour(Long tourId) {
        return bookingRepository.findByTourIdOrderByCreatedAtDesc(tourId);
    }

    @Override
    public List<Booking> getBookingsBySchedule(Long scheduleId) {
        return bookingRepository.findByScheduleIdOrderByCreatedAtDesc(scheduleId);
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
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setSpecialRequests(booking.getSpecialRequests() + "\nLý do hủy: " + reason);
        
        // Gửi email hủy
        try {
            sendBookingCancellationEmail(booking);
        } catch (Exception e) {
            log.error("Lỗi gửi email hủy booking: {}", e.getMessage());
        }
        
        return bookingRepository.save(booking);
    }

    @Override
    public BigDecimal calculateTotalAmount(Long tourId, Long scheduleId, int adultCount, int childCount) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new IllegalArgumentException("Tour không tồn tại"));
        
        TourSchedule schedule = tourScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Lịch trình không tồn tại"));
        
        // Sử dụng giá khuyến mãi nếu có, không thì dùng giá gốc
        BigDecimal adultPrice = schedule.getSpecialPrice() != null ?
                schedule.getSpecialPrice() : (tour.getAdultPrice() != null ? tour.getAdultPrice() : BigDecimal.ZERO);
        BigDecimal childPrice = tour.getChildPrice() != null ? tour.getChildPrice() : BigDecimal.ZERO;
        
        BigDecimal adultTotal = adultPrice.multiply(BigDecimal.valueOf(adultCount));
        BigDecimal childTotal = childPrice.multiply(BigDecimal.valueOf(childCount));
        
        return adultTotal.add(childTotal);
    }

    @Override
    public boolean isScheduleAvailable(Long scheduleId, int adultCount, int childCount) {
        TourSchedule schedule = tourScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Lịch trình không tồn tại"));
        
        int totalRequested = adultCount + childCount;
        Integer availableSlots = schedule.getAvailableSlots();
        if (availableSlots == null) {
            // Nếu không thiết lập số chỗ, coi như không giới hạn
            return true;
        }
        return availableSlots >= totalRequested;
    }

    @Override
    public String generateBookingCode() {
        Random random = new Random();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomPart = String.format("%04d", random.nextInt(10000));
        return "BK" + timestamp + randomPart;
    }

    private String generatePaymentCode() {
        Random random = new Random();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomPart = String.format("%04d", random.nextInt(10000));
        return "PM" + timestamp + randomPart;
    }

    @Override
    public void sendBookingConfirmationEmail(Booking booking) {
        // TODO: Implement email service
        log.info("Gửi email xác nhận booking: {}", booking.getBookingCode());
    }

    @Override
    public void sendBookingCancellationEmail(Booking booking) {
        // TODO: Implement email service
        log.info("Gửi email hủy booking: {}", booking.getBookingCode());
    }
} 