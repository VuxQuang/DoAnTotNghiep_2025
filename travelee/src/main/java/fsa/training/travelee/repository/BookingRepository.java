package fsa.training.travelee.repository;

import fsa.training.travelee.entity.booking.Booking;
import fsa.training.travelee.entity.booking.BookingStatus;
import fsa.training.travelee.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    // Tìm booking theo user
    List<Booking> findByUserOrderByCreatedAtDesc(User user);
    
    // Tìm booking theo user với phân trang
    Page<Booking> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // Tìm booking theo status
    List<Booking> findByStatusOrderByCreatedAtDesc(BookingStatus status);
    
    // Tìm booking theo tour
    List<Booking> findByTourIdOrderByCreatedAtDesc(Long tourId);
    
    // Tìm booking theo schedule
    List<Booking> findByScheduleIdOrderByCreatedAtDesc(Long scheduleId);
    
    // Tìm booking theo booking code
    Optional<Booking> findByBookingCode(String bookingCode);
    
    // Đếm số booking theo status
    long countByStatus(BookingStatus status);
    
    // Tìm booking theo user và status
    List<Booking> findByUserAndStatusOrderByCreatedAtDesc(User user, BookingStatus status);
    
    // Tìm booking theo tour và status
    List<Booking> findByTourIdAndStatusOrderByCreatedAtDesc(Long tourId, BookingStatus status);
    
    // Tìm booking theo khoảng thời gian
    @Query("SELECT b FROM Booking b WHERE b.createdAt BETWEEN :startDate AND :endDate ORDER BY b.createdAt DESC")
    List<Booking> findByCreatedAtBetween(@Param("startDate") java.time.LocalDateTime startDate, 
                                        @Param("endDate") java.time.LocalDateTime endDate);
    
    // Tìm booking theo giá trị (từ - đến)
    @Query("SELECT b FROM Booking b WHERE b.totalAmount BETWEEN :minAmount AND :maxAmount ORDER BY b.totalAmount DESC")
    List<Booking> findByTotalAmountBetween(@Param("minAmount") java.math.BigDecimal minAmount, 
                                          @Param("maxAmount") java.math.BigDecimal maxAmount);
    
    // Tìm booking theo ID với đầy đủ relationship
    @Query("SELECT b FROM Booking b WHERE b.id = :id")
    Optional<Booking> findByIdWithRelationships(@Param("id") Long id);

    // Tìm booking theo ID với participants và payment
    @Query("SELECT b FROM Booking b WHERE b.id = :id")
    Optional<Booking> findByIdWithParticipantsAndPayment(@Param("id") Long id);
} 