package fsa.training.travelee.repository;

import fsa.training.travelee.entity.Booking;
import fsa.training.travelee.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    // Tìm booking theo user
    Page<Booking> findByUserId(Long userId, Pageable pageable);
    
    // Tìm booking theo tour
    Page<Booking> findByTourId(Long tourId, Pageable pageable);
    
    // Tìm booking theo status
    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);
    
    // Tìm booking theo user và status
    Page<Booking> findByUserIdAndStatus(Long userId, BookingStatus status, Pageable pageable);
    
    // Tìm booking theo tour và status
    Page<Booking> findByTourIdAndStatus(Long tourId, BookingStatus status, Pageable pageable);
    
    // Tìm booking theo khoảng thời gian
    @Query("SELECT b FROM Booking b WHERE b.departureDate BETWEEN :startDate AND :endDate")
    List<Booking> findByDepartureDateBetween(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);
    
    // Đếm số booking theo status
    long countByStatus(BookingStatus status);
    
    // Đếm số booking theo tour và status
    long countByTourIdAndStatus(Long tourId, BookingStatus status);
    
    // Tìm booking theo email khách hàng
    List<Booking> findByCustomerEmail(String customerEmail);
    
    // Tìm booking theo số điện thoại khách hàng
    List<Booking> findByCustomerPhone(String customerPhone);
} 