package fsa.training.travelee.repository;

import fsa.training.travelee.entity.TourSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TourScheduleRepository extends JpaRepository<TourSchedule, Long> {
    
    // Tìm schedule theo tour
    List<TourSchedule> findByTourIdOrderByDepartureDateAsc(Long tourId);
    
    // Tìm schedule theo tour với phân trang
    Page<TourSchedule> findByTourIdOrderByDepartureDateAsc(Long tourId, Pageable pageable);
    
    // Tìm schedule theo status
    List<TourSchedule> findByStatusOrderByDepartureDateAsc(String status);
    
    // Tìm schedule theo ngày khởi hành
    List<TourSchedule> findByDepartureDateOrderByDepartureDateAsc(LocalDate departureDate);
    
    // Tìm schedule theo khoảng ngày khởi hành
    List<TourSchedule> findByDepartureDateBetweenOrderByDepartureDateAsc(LocalDate startDate, LocalDate endDate);
    
    // Tìm schedule theo tour và status
    List<TourSchedule> findByTourIdAndStatusOrderByDepartureDateAsc(Long tourId, String status);
    
    // Tìm schedule theo tour và ngày khởi hành
    List<TourSchedule> findByTourIdAndDepartureDateOrderByDepartureDateAsc(Long tourId, LocalDate departureDate);
    
    // Tìm schedule theo tour, status và ngày khởi hành
    List<TourSchedule> findByTourIdAndStatusAndDepartureDateOrderByDepartureDateAsc(Long tourId, String status, LocalDate departureDate);
    
    // Tìm schedule có sẵn chỗ (available)
    List<TourSchedule> findByStatusAndDepartureDateAfterOrderByDepartureDateAsc(String status, LocalDate currentDate);
    
    // Tìm schedule theo tour có sẵn chỗ
    List<TourSchedule> findByTourIdAndStatusAndDepartureDateAfterOrderByDepartureDateAsc(Long tourId, String status, LocalDate currentDate);
    
    // Đếm số schedule theo tour
    long countByTourId(Long tourId);
    
    // Đếm số schedule theo status
    long countByStatus(String status);
    
    // Đếm số schedule theo tour và status
    long countByTourIdAndStatus(Long tourId, String status);
    
    // Tìm schedule theo giá khuyến mãi (có special price)
    List<TourSchedule> findBySpecialPriceIsNotNullOrderBySpecialPriceAsc();
    
    // Tìm schedule theo tour có giá khuyến mãi
    List<TourSchedule> findByTourIdAndSpecialPriceIsNotNullOrderBySpecialPriceAsc(Long tourId);
    
    // Tìm schedule theo khoảng giá khuyến mãi
    @Query("SELECT ts FROM TourSchedule ts WHERE ts.specialPrice BETWEEN :minPrice AND :maxPrice ORDER BY ts.specialPrice ASC")
    List<TourSchedule> findBySpecialPriceBetween(@Param("minPrice") java.math.BigDecimal minPrice, 
                                                @Param("maxPrice") java.math.BigDecimal maxPrice);
    
    // Tìm schedule theo tour và khoảng giá khuyến mãi
    @Query("SELECT ts FROM TourSchedule ts WHERE ts.tour.id = :tourId AND ts.specialPrice BETWEEN :minPrice AND :maxPrice ORDER BY ts.specialPrice ASC")
    List<TourSchedule> findByTourIdAndSpecialPriceBetween(@Param("tourId") Long tourId,
                                                         @Param("minPrice") java.math.BigDecimal minPrice, 
                                                         @Param("maxPrice") java.math.BigDecimal maxPrice);
    
    // Tìm schedule sắp khởi hành (trong vòng 7 ngày tới)
    @Query("SELECT ts FROM TourSchedule ts WHERE ts.departureDate BETWEEN :today AND :nextWeek AND ts.status = 'available' ORDER BY ts.departureDate ASC")
    List<TourSchedule> findUpcomingSchedules(@Param("today") LocalDate today, 
                                            @Param("nextWeek") LocalDate nextWeek);
    
    // Tìm schedule theo tour sắp khởi hành
    @Query("SELECT ts FROM TourSchedule ts WHERE ts.tour.id = :tourId AND ts.departureDate BETWEEN :today AND :nextWeek AND ts.status = 'available' ORDER BY ts.departureDate ASC")
    List<TourSchedule> findUpcomingSchedulesByTour(@Param("tourId") Long tourId,
                                                  @Param("today") LocalDate today, 
                                                  @Param("nextWeek") LocalDate nextWeek);
    
    // Tìm schedule có ít chỗ còn lại (limited)
    @Query("SELECT ts FROM TourSchedule ts WHERE ts.status = 'limited' ORDER BY ts.departureDate ASC")
    List<TourSchedule> findLimitedSchedules();
    
    // Tìm schedule theo tour có ít chỗ còn lại
    @Query("SELECT ts FROM TourSchedule ts WHERE ts.tour.id = :tourId AND ts.status = 'limited' ORDER BY ts.departureDate ASC")
    List<TourSchedule> findLimitedSchedulesByTour(@Param("tourId") Long tourId);
}
