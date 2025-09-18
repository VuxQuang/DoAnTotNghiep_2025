package fsa.training.travelee.service;

import fsa.training.travelee.entity.review.Review;
import fsa.training.travelee.entity.review.ReviewStatus;
import fsa.training.travelee.entity.Tour;
import fsa.training.travelee.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReviewService {

    // Tạo review mới (chỉ cho user đã thanh toán tour)
    Review createReview(Long tourId, int rating, String comment, User user);

    // Lấy reviews theo tour (chỉ status APPROVED)
    List<Review> getReviewsByTour(Long tourId);

    // Lấy reviews theo tour với phân trang (chỉ status APPROVED)
    Page<Review> getReviewsByTour(Long tourId, Pageable pageable);

    // Lấy reviews theo user
    List<Review> getReviewsByUser(User user);

    // Lấy reviews theo user với phân trang
    Page<Review> getReviewsByUser(User user, Pageable pageable);

    // Lấy review theo ID
    Review getReviewById(Long id);

    // Cập nhật review (chỉ user sở hữu review)
    Review updateReview(Long reviewId, int rating, String comment, User user);

    // Xóa review (chỉ user sở hữu review)
    void deleteReview(Long reviewId, User user);

    // Kiểm tra user đã review tour chưa
    boolean hasUserReviewedTour(Long userId, Long tourId);

    // Kiểm tra user có thể review tour không (đã thanh toán)
    boolean canUserReviewTour(Long userId, Long tourId);

    // Lấy review của user cho tour cụ thể
    Review getUserReviewForTour(Long userId, Long tourId);

    // Lấy tất cả reviews của user cho tour cụ thể
    List<Review> getUserReviewsForTour(Long userId, Long tourId);

    // Tính điểm trung bình rating của tour
    Double getAverageRating(Long tourId);

    // Đếm số reviews của tour
    long getReviewCount(Long tourId);

    // Admin: Lấy tất cả reviews với phân trang
    Page<Review> getAllReviews(Pageable pageable);

    // Admin: Lấy reviews theo status
    Page<Review> getReviewsByStatus(ReviewStatus status, Pageable pageable);

    // Admin: Tìm kiếm reviews
    Page<Review> searchReviews(String keyword, Pageable pageable);

    // Admin: Cập nhật status review
    Review updateReviewStatus(Long reviewId, ReviewStatus status);

    // Admin: Phản hồi review
    Review respondToReview(Long reviewId, String adminResponse);

    // Admin: Xóa review
    void deleteReviewByAdmin(Long reviewId);
}
