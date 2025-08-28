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
import fsa.training.travelee.repository.PaymentRepository;
import fsa.training.travelee.repository.TourScheduleRepository;
import fsa.training.travelee.service.EmailService;
import fsa.training.travelee.service.PromotionService;
import fsa.training.travelee.repository.PromotionRepository;
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
    private final EmailService emailService;
    private final PaymentRepository paymentRepository;
    private final PromotionService promotionService;
    private final PromotionRepository promotionRepository;

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

        // Tính tổng tiền gốc
        BigDecimal subtotal = calculateTotalAmount(
                bookingRequest.getTourId(),
                bookingRequest.getScheduleId(),
                bookingRequest.getAdultCount(),
                bookingRequest.getChildCount()
        );

        // Áp dụng giảm giá nếu có mã (tính lại server-side để tránh làm giả)
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (bookingRequest.getPromotionCode() != null && !bookingRequest.getPromotionCode().isBlank()) {
            try {
                var validation = promotionService.validatePromotionCode(
                        bookingRequest.getPromotionCode(),
                        bookingRequest.getTourId(),
                        subtotal
                );
                if (validation.isSuccess() && validation.getDiscountAmount() != null) {
                    discountAmount = validation.getDiscountAmount();
                }
            } catch (Exception ignored) {
                discountAmount = BigDecimal.ZERO;
            }
        }
        if (discountAmount.compareTo(BigDecimal.ZERO) < 0) discountAmount = BigDecimal.ZERO;
        if (discountAmount.compareTo(subtotal) > 0) discountAmount = subtotal;
        BigDecimal totalAmount = subtotal.subtract(discountAmount);

        // Tạo booking
        Booking booking = Booking.builder()
                .bookingCode(generateBookingCode())
                .adultCount(bookingRequest.getAdultCount())
                .childCount(bookingRequest.getChildCount())
                .totalAmount(totalAmount)
                .discountAmount(discountAmount)
                .specialRequests(bookingRequest.getSpecialRequests())
                .status(BookingStatus.PENDING)
                .user(user)
                .tour(tour)
                .schedule(schedule)
                .build();
        booking.setDiscountAmount(discountAmount);
        if (bookingRequest.getPromotionCode() != null && !bookingRequest.getPromotionCode().isBlank()) {
            promotionRepository.findByCode(bookingRequest.getPromotionCode()).ifPresent(booking::setPromotion);
        }

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

        // Cập nhật số chỗ còn lại nếu booking được tạo với trạng thái khác PENDING
        if (finalBooking.getStatus() != BookingStatus.PENDING) {
            // Kiểm tra lại số chỗ còn lại trước khi trừ
            if (!isScheduleAvailable(bookingRequest.getScheduleId(), 
                    bookingRequest.getAdultCount(), 
                    bookingRequest.getChildCount())) {
                throw new IllegalStateException("Không đủ chỗ để xác nhận booking này");
            }
            updateAvailableSlots(finalBooking, null, finalBooking.getStatus());
        }

        // Gửi email xác nhận
        try {
            emailService.sendBookingConfirmationEmail(finalBooking);
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
        // Sử dụng method phân trang và convert sang List
        Page<Booking> page = bookingRepository.findByStatusOrderByCreatedAtDesc(status, Pageable.unpaged());
        return page.getContent();
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
        BookingStatus oldStatus = booking.getStatus();
        booking.setStatus(status);
        
        // Cập nhật số chỗ còn lại dựa trên thay đổi trạng thái
        // Kiểm tra số chỗ còn lại trước khi cập nhật (nếu cần trừ chỗ)
        if ((status == BookingStatus.CONFIRMED && oldStatus == BookingStatus.PENDING) ||
            (status == BookingStatus.PAID && oldStatus == BookingStatus.PENDING) ||
            (status == BookingStatus.CONFIRMED && oldStatus == BookingStatus.CANCELLED) ||
            (status == BookingStatus.PAID && oldStatus == BookingStatus.CANCELLED)) {
            
            if (!isScheduleAvailable(booking.getSchedule().getId(), 
                    booking.getAdultCount(), 
                    booking.getChildCount())) {
                throw new IllegalStateException("Không đủ chỗ để cập nhật trạng thái booking này");
            }
        }
        
        updateAvailableSlots(booking, oldStatus, status);
        
        // Đồng bộ trạng thái thanh toán khi booking được thanh toán/hoàn tất/được xác nhận
        if (status == BookingStatus.PAID || status == BookingStatus.COMPLETED || status == BookingStatus.CONFIRMED) {
            Payment payment = booking.getPayment();
            if (payment != null && payment.getStatus() != PaymentStatus.COMPLETED) {
                payment.setStatus(PaymentStatus.COMPLETED);
                if (payment.getPaidAt() == null) {
                    payment.setPaidAt(LocalDateTime.now());
                }
                booking.setPayment(payment);
            }
        }

                    try {
                        if (status == BookingStatus.CONFIRMED && oldStatus == BookingStatus.PENDING) {
                            // Nếu xác nhận từ pending thì gửi email xác nhận
                            emailService.sendBookingConfirmationEmail(booking);
                        } else if (status == BookingStatus.PAID) {
                            // Nếu đánh dấu đã thanh toán (admin chọn Paid)
                            emailService.sendBookingPaidEmail(booking);
                        } else if (status == BookingStatus.COMPLETED) {
                            // Nếu hoàn thành tour thì gửi email cảm ơn
                            emailService.sendBookingCompletionEmail(booking);
                        } else if (status == BookingStatus.REFUNDED) {
                            // Nếu đánh dấu hoàn tiền (trường hợp hiếm khi set trực tiếp)
                            emailService.sendBookingRefundEmail(booking);
                        }
                        // Không gửi email cho các thay đổi trạng thái khác
                    } catch (Exception e) {
                        log.error("Lỗi gửi email thông báo thay đổi trạng thái booking: {}", e.getMessage());
                    }
        
        return bookingRepository.save(booking);
    }

    @Override
    public Booking cancelBooking(Long bookingId, String reason) {
        Booking booking = getBookingById(bookingId);
        // Không cho hủy nếu đã thanh toán hoặc đã hoàn thành
        if (booking.getStatus() == BookingStatus.PAID || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException("Không thể hủy vì booking đã thanh toán hoặc đã hoàn thành");
        }

        BookingStatus oldStatus = booking.getStatus();
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setSpecialRequests(booking.getSpecialRequests() + "\nLý do hủy: " + reason);

        // Cập nhật số chỗ còn lại khi hủy
        updateAvailableSlots(booking, oldStatus, BookingStatus.CANCELLED);

        // Gửi email hủy
        try {
            emailService.sendBookingCancellationEmail(booking);
        } catch (Exception e) {
            log.error("Lỗi gửi email hủy booking: {}", e.getMessage());
        }

        return bookingRepository.save(booking);
    }

    @Override
    public Booking cancelBookingByAdmin(Long bookingId, String reason) {
        Booking booking = getBookingById(bookingId);
        BookingStatus oldStatus = booking.getStatus();
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setSpecialRequests((booking.getSpecialRequests() != null ? booking.getSpecialRequests() : "")
                + "\nAdmin hủy: " + reason);

        // Cập nhật số chỗ còn lại khi hủy
        updateAvailableSlots(booking, oldStatus, BookingStatus.CANCELLED);

        try {
            emailService.sendBookingCancellationEmail(booking);
        } catch (Exception e) {
            log.error("Lỗi gửi email hủy booking: {}", e.getMessage());
        }

        return bookingRepository.save(booking);
    }

    @Override
    public Booking refundBooking(Long bookingId, BigDecimal amount, String reason) {
        Booking booking = getBookingById(bookingId);
        Payment payment = booking.getPayment();
        if (payment == null || payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Không thể hoàn tiền vì booking chưa thanh toán");
        }

        // Cập nhật payment
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundedAt(LocalDateTime.now());
        payment.setRefundAmount(amount);
        payment.setRefundReason(reason);
        paymentRepository.save(payment);
        booking.setPayment(payment);

        // Cập nhật booking
        BookingStatus oldStatus = booking.getStatus();
        booking.setStatus(BookingStatus.REFUNDED);
        booking.setSpecialRequests((booking.getSpecialRequests() != null ? booking.getSpecialRequests() : "")
                + "\nRefund: " + reason + ", amount=" + amount);

        // Cập nhật số chỗ còn lại khi refund (hoàn tác số chỗ đã trừ)
        updateAvailableSlots(booking, oldStatus, BookingStatus.REFUNDED);

        Booking saved = bookingRepository.save(booking);
        try {
            emailService.sendBookingRefundEmail(saved);
        } catch (Exception e) {
            log.error("Lỗi gửi email hoàn tiền: {}", e.getMessage());
        }
        return saved;
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
    public Page<Booking> getAllBookingsWithPagination(Pageable pageable) {
        return bookingRepository.findAll(pageable);
    }

    @Override
    public Page<Booking> getBookingsByStatusWithPagination(BookingStatus status, Pageable pageable) {
        return bookingRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
    }

    @Override
    public Page<Booking> searchBookings(String searchTerm, Pageable pageable) {
        return bookingRepository.searchBookings(searchTerm, pageable);
    }

    @Override
    public Page<Booking> getBookingsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return bookingRepository.findByCreatedAtBetween(startDate, endDate, pageable);
    }

    @Override
    public long getTotalBookings() {
        return bookingRepository.count();
    }

    @Override
    public long getTotalBookingsByStatus(BookingStatus status) {
        return bookingRepository.countByStatus(status);
    }

    /**
     * Cập nhật số chỗ còn lại dựa trên thay đổi trạng thái booking
     * @param booking Booking cần cập nhật
     * @param oldStatus Trạng thái cũ (có thể null khi tạo booking mới)
     * @param newStatus Trạng thái mới
     */
    private void updateAvailableSlots(Booking booking, BookingStatus oldStatus, BookingStatus newStatus) {
        TourSchedule schedule = booking.getSchedule();
        if (schedule == null || schedule.getAvailableSlots() == null) {
            return; // Không có schedule hoặc không giới hạn số chỗ
        }

        int totalParticipants = booking.getAdultCount() + booking.getChildCount();
        int currentAvailableSlots = schedule.getAvailableSlots();

        // Xác định khi nào cần trừ hoặc cộng số chỗ
        boolean shouldDecreaseSlots = false;
        boolean shouldIncreaseSlots = false;

        // Trường hợp 0: Tạo booking mới với trạng thái khác PENDING
        if (oldStatus == null) {
            if (newStatus == BookingStatus.CONFIRMED || newStatus == BookingStatus.PAID) {
                shouldDecreaseSlots = true;
            }
        }
        // Trường hợp 1: Từ PENDING -> CONFIRMED (xác nhận giữ chỗ)
        else if (oldStatus == BookingStatus.PENDING && newStatus == BookingStatus.CONFIRMED) {
            shouldDecreaseSlots = true;
        }
        // Trường hợp 2: Từ PENDING -> PAID (thanh toán trực tiếp)
        else if (oldStatus == BookingStatus.PENDING && newStatus == BookingStatus.PAID) {
            shouldDecreaseSlots = true;
        }
        // Trường hợp 3: Từ CONFIRMED -> PAID (thanh toán sau khi xác nhận)
        else if (oldStatus == BookingStatus.CONFIRMED && newStatus == BookingStatus.PAID) {
            // Không cần thay đổi vì đã trừ từ trước
        }
        // Trường hợp 4: Từ bất kỳ trạng thái nào -> CANCELLED (hủy booking)
        else if (newStatus == BookingStatus.CANCELLED) {
            // Chỉ hoàn tác số chỗ nếu trước đó đã trừ (CONFIRMED, PAID, hoặc COMPLETED)
            if (oldStatus == BookingStatus.CONFIRMED || oldStatus == BookingStatus.PAID || oldStatus == BookingStatus.COMPLETED) {
                shouldIncreaseSlots = true;
            }
        }
        // Trường hợp 5: Từ CANCELLED -> CONFIRMED (khôi phục booking đã hủy)
        else if (oldStatus == BookingStatus.CANCELLED && newStatus == BookingStatus.CONFIRMED) {
            shouldDecreaseSlots = true;
        }
        // Trường hợp 6: Từ CANCELLED -> PAID (khôi phục và thanh toán trực tiếp)
        else if (oldStatus == BookingStatus.CANCELLED && newStatus == BookingStatus.PAID) {
            shouldDecreaseSlots = true;
        }
        // Trường hợp 7: Từ PAID -> REFUNDED (hoàn tiền)
        else if (oldStatus == BookingStatus.PAID && newStatus == BookingStatus.REFUNDED) {
            shouldIncreaseSlots = true;
        }
        // Trường hợp 8: Từ CONFIRMED -> REFUNDED (hoàn tiền trước khi thanh toán)
        else if (oldStatus == BookingStatus.CONFIRMED && newStatus == BookingStatus.REFUNDED) {
            shouldIncreaseSlots = true;
        }
        // Trường hợp 9: Từ PAID -> COMPLETED (hoàn thành tour)
        else if (oldStatus == BookingStatus.PAID && newStatus == BookingStatus.COMPLETED) {
            // Không cần thay đổi số chỗ vì đây là trạng thái cuối cùng
        }
        // Trường hợp 10: Từ CONFIRMED -> COMPLETED (hoàn thành tour mà chưa thanh toán)
        else if (oldStatus == BookingStatus.CONFIRMED && newStatus == BookingStatus.COMPLETED) {
            // Không cần thay đổi số chỗ vì đây là trạng thái cuối cùng
        }

        // Thực hiện cập nhật số chỗ
        if (shouldDecreaseSlots) {
            if (currentAvailableSlots >= totalParticipants) {
                schedule.setAvailableSlots(currentAvailableSlots - totalParticipants);
                log.info("Đã trừ {} chỗ từ lịch trình {} (còn lại: {}) - Booking {}: {} -> {}", 
                    totalParticipants, schedule.getId(), schedule.getAvailableSlots(), 
                    booking.getId(), oldStatus, newStatus);
            } else {
                log.warn("Không đủ chỗ để trừ: yêu cầu {}, có sẵn {} - Booking {}: {} -> {}", 
                    totalParticipants, currentAvailableSlots, 
                    booking.getId(), oldStatus, newStatus);
            }
        } else if (shouldIncreaseSlots) {
            schedule.setAvailableSlots(currentAvailableSlots + totalParticipants);
            log.info("Đã hoàn tác {} chỗ cho lịch trình {} (còn lại: {}) - Booking {}: {} -> {}", 
                totalParticipants, schedule.getId(), schedule.getAvailableSlots(), 
                booking.getId(), oldStatus, newStatus);
        } else {
            log.debug("Không cần thay đổi số chỗ - Booking {}: {} -> {}", 
                booking.getId(), oldStatus, newStatus);
        }

        // Lưu thay đổi vào database
        if (shouldDecreaseSlots || shouldIncreaseSlots) {
            tourScheduleRepository.save(schedule);
        }
    }
}