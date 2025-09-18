package fsa.training.travelee.repository;

import fsa.training.travelee.entity.booking.Booking;
import fsa.training.travelee.entity.booking.BookingStatus;
import fsa.training.travelee.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Tìm booking theo user
    List<Booking> findByUserOrderByCreatedAtDesc(User user);

    // Tìm booking theo user với phân trang
    Page<Booking> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    // Tìm booking theo tour
    List<Booking> findByTourIdOrderByCreatedAtDesc(Long tourId);

    // Tìm booking theo tour với phân trang
    Page<Booking> findByTourIdOrderByCreatedAtDesc(Long tourId, Pageable pageable);

    // Tìm booking theo schedule
    List<Booking> findByScheduleIdOrderByCreatedAtDesc(Long scheduleId);

    // Tìm booking theo schedule với phân trang
    Page<Booking> findByScheduleIdOrderByCreatedAtDesc(Long scheduleId, Pageable pageable);

    // Tìm booking theo booking code
    Optional<Booking> findByBookingCode(String bookingCode);

    // Đếm số booking theo status
    long countByStatus(BookingStatus status);

    // Tìm booking theo user và status
    List<Booking> findByUserAndStatusOrderByCreatedAtDesc(User user, BookingStatus status);

    // Tìm booking theo tour và status
    List<Booking> findByTourIdAndStatusOrderByCreatedAtDesc(Long tourId, BookingStatus status);

    // Tìm booking theo user, tourId và status
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.tour.id = :tourId AND b.status = :status ORDER BY b.createdAt DESC")
    List<Booking> findByUserAndTourIdAndStatusOrderByCreatedAtDesc(@Param("userId") Long userId, 
                                                                   @Param("tourId") Long tourId, 
                                                                   @Param("status") BookingStatus status);

    // Tìm tất cả booking theo user và tourId (không filter theo status)
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.tour.id = :tourId ORDER BY b.createdAt DESC")
    List<Booking> findByUserAndTourIdOrderByCreatedAtDesc(@Param("userId") Long userId, 
                                                          @Param("tourId") Long tourId);

    // Tìm booking theo giá trị (từ - đến)
    @Query("SELECT b FROM Booking b WHERE b.totalAmount BETWEEN :minAmount AND :maxAmount ORDER BY b.totalAmount DESC")
    List<Booking> findByTotalAmountBetween(@Param("minAmount") BigDecimal minAmount,
                                           @Param("maxAmount") BigDecimal maxAmount);

    // Tìm booking theo ID với đầy đủ relationship
    @Query("SELECT b FROM Booking b WHERE b.id = :id")
    Optional<Booking> findByIdWithRelationships(@Param("id") Long id);

    // Tìm booking theo ID với participants và payment
    @Query("SELECT b FROM Booking b WHERE b.id = :id")
    Optional<Booking> findByIdWithParticipantsAndPayment(@Param("id") Long id);

    // Tìm kiếm booking theo từ khóa (mã booking, tên khách hàng, email)
    @Query("SELECT b FROM Booking b " +
           "JOIN b.user u " +
           "WHERE b.bookingCode LIKE %:searchTerm% " +
           "OR u.fullName LIKE %:searchTerm% " +
           "OR u.email LIKE %:searchTerm% " +
           "ORDER BY b.createdAt DESC")
    Page<Booking> searchBookings(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Tìm booking theo status với phân trang
    Page<Booking> findByStatusOrderByCreatedAtDesc(BookingStatus status, Pageable pageable);

    // Tìm booking theo khoảng thời gian với phân trang
    @Query("SELECT b FROM Booking b WHERE b.createdAt BETWEEN :startDate AND :endDate ORDER BY b.createdAt DESC")
    Page<Booking> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate,
                                         Pageable pageable);

    // Đếm số booking trong ngày
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.createdAt >= :startDate AND b.createdAt < :endDate")
    long countByCreatedAtDate(@Param("startDate") LocalDateTime startDate,
                              @Param("endDate") LocalDateTime endDate);

    // Đếm số booking theo tháng
    @Query("SELECT COUNT(b) FROM Booking b " +
           "WHERE FUNCTION('YEAR', b.createdAt) = :year " +
           "AND FUNCTION('MONTH', b.createdAt) = :month")
    long countByYearAndMonth(@Param("year") int year, @Param("month") int month);

    // Đếm số booking theo ngày cụ thể
    @Query("SELECT COUNT(b) FROM Booking b " +
           "WHERE FUNCTION('YEAR', b.createdAt) = :year " +
           "AND FUNCTION('MONTH', b.createdAt) = :month " +
           "AND FUNCTION('DAY', b.createdAt) = :day")
    long countByYearMonthAndDay(@Param("year") int year, @Param("month") int month, @Param("day") int day);

    // Tính doanh thu theo tháng (chỉ booking đã hoàn thành)
    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b " +
           "WHERE FUNCTION('YEAR', b.createdAt) = :year " +
           "AND FUNCTION('MONTH', b.createdAt) = :month " +
           "AND b.status = 'COMPLETED'")
    BigDecimal getRevenueByYearAndMonth(@Param("year") int year, @Param("month") int month);

    // Lấy thống kê booking theo ngày trong tháng
    @Query("SELECT CAST(b.createdAt AS date), " +
           "COUNT(b), " +
           "COALESCE(SUM(CASE WHEN b.status = 'COMPLETED' THEN b.totalAmount ELSE 0 END), 0) " +
           "FROM Booking b " +
           "WHERE FUNCTION('YEAR', b.createdAt) = :year " +
           "AND FUNCTION('MONTH', b.createdAt) = :month " +
           "GROUP BY CAST(b.createdAt AS date) " +
           "ORDER BY CAST(b.createdAt AS date)")
    List<Object[]> getMonthlyStatsByDay(@Param("year") int year, @Param("month") int month);
}
