package fsa.training.travelee.controller.page;

import fsa.training.travelee.entity.User;
import fsa.training.travelee.entity.review.Review;
import fsa.training.travelee.service.ReviewService;
import fsa.training.travelee.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;

    // Test method để kiểm tra user authentication
    @GetMapping("/test-auth")
    @ResponseBody
    public String testAuth(Principal principal) {
        User currentUser = userService.getCurrentUser();
        return String.format("Principal: %s, Current User: %s", 
                principal != null ? principal.getName() : "null",
                currentUser != null ? currentUser.getUsername() : "null");
    }

    // Form submission để tạo review mới
    @PostMapping("/create")
    public String createReview(
            @RequestParam Long tourId,
            @RequestParam int rating,
            @RequestParam String comment,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Lấy user hiện tại
            User currentUser = userService.getCurrentUser();
            
            log.info("Principal: {}, Current User: {}", principal != null ? principal.getName() : "null", 
                    currentUser != null ? currentUser.getUsername() : "null");
            
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("error", "Bạn cần đăng nhập để đánh giá");
                return "redirect:/page/tours/" + tourId;
            }

            // Kiểm tra user có thể review không
            if (!reviewService.canUserReviewTour(currentUser.getId(), tourId)) {
                redirectAttributes.addFlashAttribute("error", "Bạn chưa thanh toán hoặc hoàn thành tour này");
                return "redirect:/page/tours/" + tourId;
            }

            Review review = reviewService.createReview(tourId, rating, comment, currentUser);
            redirectAttributes.addFlashAttribute("success", "Đánh giá của bạn đã được gửi và hiển thị ngay lập tức");
            
        } catch (Exception e) {
            log.error("Lỗi khi tạo review: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/page/tours/" + tourId;
    }

    // Form submission để cập nhật review
    @PostMapping("/update/{reviewId}")
    public String updateReview(
            @PathVariable Long reviewId,
            @RequestParam Long tourId,
            @RequestParam int rating,
            @RequestParam String comment,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        
        try {
            User currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("error", "Bạn cần đăng nhập");
                return "redirect:/page/tours/" + tourId;
            }

            Review review = reviewService.updateReview(reviewId, rating, comment, currentUser);
            redirectAttributes.addFlashAttribute("success", "Đánh giá đã được cập nhật và hiển thị ngay lập tức");
            
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật review: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/page/tours/" + tourId;
    }

    // Form submission để xóa review
    @PostMapping("/delete/{reviewId}")
    public String deleteReview(
            @PathVariable Long reviewId,
            @RequestParam Long tourId,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        
        try {
            User currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("error", "Bạn cần đăng nhập");
                return "redirect:/page/tours/" + tourId;
            }

            reviewService.deleteReview(reviewId, currentUser);
            redirectAttributes.addFlashAttribute("success", "Đánh giá đã được xóa");
            
        } catch (Exception e) {
            log.error("Lỗi khi xóa review: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/page/tours/" + tourId;
    }
}