package fsa.training.travelee.service;

import fsa.training.travelee.entity.booking.Booking;
import fsa.training.travelee.entity.booking.BookingStatus;
import fsa.training.travelee.entity.review.Review;
import fsa.training.travelee.entity.review.ReviewStatus;
import fsa.training.travelee.entity.Tour;
import fsa.training.travelee.entity.User;
import fsa.training.travelee.repository.BookingRepository;
import fsa.training.travelee.repository.ReviewRepository;
import fsa.training.travelee.repository.TourRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final TourRepository tourRepository;
    private final BookingRepository bookingRepository;

    @Override
    public Review createReview(Long tourId, int rating, String comment, User user) {
        // Kiểm tra user có thể review tour không
        if (!canUserReviewTour(user.getId(), tourId)) {
            throw new IllegalStateException("Bạn chưa thanh toán hoặc hoàn thành tour này");
        }

        // Kiểm tra rating hợp lệ
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating phải từ 1 đến 5 sao");
        }

        // Lấy tour
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tour"));

        // Lấy booking đã thanh toán hoặc hoàn thành mới nhất của user cho tour này
        List<Booking> paidBookings = bookingRepository.findByUserAndTourIdAndStatusOrderByCreatedAtDesc(user.getId(), tourId, BookingStatus.PAID);
        List<Booking> completedBookings = bookingRepository.findByUserAndTourIdAndStatusOrderByCreatedAtDesc(user.getId(), tourId, BookingStatus.COMPLETED);
        
        Booking eligibleBooking = Stream.concat(paidBookings.stream(), completedBookings.stream())
                .sorted((b1, b2) -> b2.getCreatedAt().compareTo(b1.getCreatedAt()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking đã thanh toán hoặc hoàn thành"));

        // Kiểm tra xem đã có review cho booking này chưa
        Optional<Review> existingReview = reviewRepository.findByBookingId(eligibleBooking.getId());
        if (existingReview.isPresent()) {
            // Nếu đã có review, cập nhật review đó thay vì tạo mới
            Review review = existingReview.get();
            review.setRating(rating);
            review.setComment(comment);
            review.setStatus(ReviewStatus.APPROVED); // Hiển thị ngay lập tức
            review.setUpdatedAt(LocalDateTime.now());
            return reviewRepository.save(review);
        }

        // Tạo review mới
        Review review = Review.builder()
                .rating(rating)
                .comment(comment)
                .status(ReviewStatus.APPROVED) // Hiển thị ngay lập tức
                .user(user)
                .tour(tour)
                .booking(eligibleBooking)
                .build();

        return reviewRepository.save(review);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> getReviewsByTour(Long tourId) {
        return reviewRepository.findByTourIdAndStatusApproved(tourId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> getReviewsByTour(Long tourId, Pageable pageable) {
        return reviewRepository.findByTourIdAndStatusApproved(tourId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> getReviewsByUser(User user) {
        return reviewRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> getReviewsByUser(User user, Pageable pageable) {
        return reviewRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Review getReviewById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy review"));
    }

    @Override
    public Review updateReview(Long reviewId, int rating, String comment, User user) {
        Review review = getReviewById(reviewId);
        
        // Kiểm tra quyền sở hữu
        if (!review.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("Bạn không có quyền chỉnh sửa review này");
        }

        // Kiểm tra rating hợp lệ
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating phải từ 1 đến 5 sao");
        }

        review.setRating(rating);
        review.setComment(comment);
        review.setStatus(ReviewStatus.PENDING); // Reset về pending khi chỉnh sửa

        return reviewRepository.save(review);
    }

    @Override
    public void deleteReview(Long reviewId, User user) {
        Review review = getReviewById(reviewId);
        
        // Kiểm tra quyền sở hữu
        if (!review.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("Bạn không có quyền xóa review này");
        }

        reviewRepository.delete(review);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserReviewedTour(Long userId, Long tourId) {
        // Bây giờ cho phép nhiều review cho cùng một tour, nên luôn trả về false
        // để cho phép user tạo review mới
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUserReviewTour(Long userId, Long tourId) {
        log.info("Checking if user {} can review tour {}", userId, tourId);

        // Kiểm tra user có booking đã thanh toán (PAID) hoặc hoàn thành (COMPLETED) cho tour này không
        List<Booking> paidBookings = bookingRepository.findByUserAndTourIdAndStatusOrderByCreatedAtDesc(
                userId, tourId, BookingStatus.PAID);
        List<Booking> completedBookings = bookingRepository.findByUserAndTourIdAndStatusOrderByCreatedAtDesc(
                userId, tourId, BookingStatus.COMPLETED);
        
        log.info("Found {} paid bookings and {} completed bookings for user {} and tour {}", 
                paidBookings.size(), completedBookings.size(), userId, tourId);
        
        // Log tất cả bookings của user cho tour này (để debug)
        List<Booking> allBookings = bookingRepository.findByUserAndTourIdOrderByCreatedAtDesc(userId, tourId);
        log.info("All bookings for user {} and tour {}: {}", userId, tourId, 
                allBookings.stream().map(b -> "ID:" + b.getId() + ", Status:" + b.getStatus()).toList());
        
        // Chỉ cần có booking đã thanh toán hoặc hoàn thành là có thể review
        return !paidBookings.isEmpty() || !completedBookings.isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public Review getUserReviewForTour(Long userId, Long tourId) {
        // Trả về review mới nhất của user cho tour này (để tương thích với code cũ)
        return reviewRepository.findByUserIdAndTourIdOrderByCreatedAtDesc(userId, tourId)
                .stream()
                .findFirst()
                .orElse(null);
    }

    // Thêm method mới để lấy tất cả reviews của user cho tour
    @Transactional(readOnly = true)
    public List<Review> getUserReviewsForTour(Long userId, Long tourId) {
        return reviewRepository.findByUserIdAndTourIdOrderByCreatedAtDesc(userId, tourId);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageRating(Long tourId) {
        return reviewRepository.getAverageRatingByTourId(tourId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getReviewCount(Long tourId) {
        return reviewRepository.countByTourIdAndStatus(tourId, ReviewStatus.APPROVED);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> getAllReviews(Pageable pageable) {
        return reviewRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> getReviewsByStatus(ReviewStatus status, Pageable pageable) {
        return reviewRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> searchReviews(String keyword, Pageable pageable) {
        return reviewRepository.searchReviews(keyword, pageable);
    }

    @Override
    public Review updateReviewStatus(Long reviewId, ReviewStatus status) {
        Review review = getReviewById(reviewId);
        review.setStatus(status);
        return reviewRepository.save(review);
    }

    @Override
    public Review respondToReview(Long reviewId, String adminResponse) {
        Review review = getReviewById(reviewId);
        review.setAdminResponse(adminResponse);
        return reviewRepository.save(review);
    }

    @Override
    public void deleteReviewByAdmin(Long reviewId) {
        Review review = getReviewById(reviewId);
        reviewRepository.delete(review);
    }
}
