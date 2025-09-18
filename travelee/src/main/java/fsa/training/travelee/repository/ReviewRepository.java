package fsa.training.travelee.repository;

import fsa.training.travelee.entity.review.Review;
import fsa.training.travelee.entity.review.ReviewStatus;
import fsa.training.travelee.entity.Tour;
import fsa.training.travelee.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Tìm reviews theo tour với status APPROVED
    @Query("SELECT r FROM Review r WHERE r.tour.id = :tourId AND r.status = 'APPROVED' ORDER BY r.createdAt DESC")
    List<Review> findByTourIdAndStatusApproved(@Param("tourId") Long tourId);

    // Tìm reviews theo tour với status APPROVED và phân trang
    @Query("SELECT r FROM Review r WHERE r.tour.id = :tourId AND r.status = 'APPROVED' ORDER BY r.createdAt DESC")
    Page<Review> findByTourIdAndStatusApproved(@Param("tourId") Long tourId, Pageable pageable);

    // Tìm review theo user và tour (để kiểm tra user đã review chưa)
    @Query("SELECT r FROM Review r WHERE r.user.id = :userId AND r.tour.id = :tourId")
    Optional<Review> findByUserIdAndTourId(@Param("userId") Long userId, @Param("tourId") Long tourId);

    // Tìm tất cả reviews của user cho tour (sắp xếp theo thời gian tạo mới nhất)
    @Query("SELECT r FROM Review r WHERE r.user.id = :userId AND r.tour.id = :tourId ORDER BY r.createdAt DESC")
    List<Review> findByUserIdAndTourIdOrderByCreatedAtDesc(@Param("userId") Long userId, @Param("tourId") Long tourId);

    // Tìm review theo booking (để kiểm tra booking đã có review chưa)
    @Query("SELECT r FROM Review r WHERE r.booking.id = :bookingId")
    Optional<Review> findByBookingId(@Param("bookingId") Long bookingId);

    // Tìm reviews theo user
    List<Review> findByUserOrderByCreatedAtDesc(User user);

    // Tìm reviews theo user với phân trang
    Page<Review> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    // Tìm reviews theo status
    List<Review> findByStatusOrderByCreatedAtDesc(ReviewStatus status);

    // Tìm reviews theo status với phân trang
    Page<Review> findByStatusOrderByCreatedAtDesc(ReviewStatus status, Pageable pageable);

    // Đếm số reviews theo tour và status
    @Query("SELECT COUNT(r) FROM Review r WHERE r.tour.id = :tourId AND r.status = :status")
    long countByTourIdAndStatus(@Param("tourId") Long tourId, @Param("status") ReviewStatus status);

    // Tính điểm trung bình rating theo tour
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.tour.id = :tourId AND r.status = 'APPROVED'")
    Double getAverageRatingByTourId(@Param("tourId") Long tourId);

    // Tìm reviews theo tour với tất cả status (cho admin)
    List<Review> findByTourOrderByCreatedAtDesc(Tour tour);

    // Tìm reviews theo tour với tất cả status và phân trang (cho admin)
    Page<Review> findByTourOrderByCreatedAtDesc(Tour tour, Pageable pageable);

    // Tìm kiếm reviews (cho admin)
    @Query("SELECT r FROM Review r " +
           "JOIN r.user u " +
           "JOIN r.tour t " +
           "WHERE (:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(r.comment) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY r.createdAt DESC")
    Page<Review> searchReviews(@Param("keyword") String keyword, Pageable pageable);
}
