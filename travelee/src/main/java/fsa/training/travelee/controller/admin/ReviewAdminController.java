package fsa.training.travelee.controller.admin;

import fsa.training.travelee.entity.review.Review;
import fsa.training.travelee.entity.review.ReviewStatus;
import fsa.training.travelee.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewAdminController {

    private final ReviewService reviewService;

    @GetMapping
    public String listReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Page<Review> reviews;
        
        if (search != null && !search.trim().isEmpty()) {
            reviews = reviewService.searchReviews(search, pageable);
        } else {
            reviews = reviewService.getAllReviews(pageable);
        }

        model.addAttribute("reviews", reviews);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reviews.getTotalPages());
        model.addAttribute("totalItems", reviews.getTotalElements());
        model.addAttribute("searchTerm", search);

        return "admin/review/review-list";
    }

    @GetMapping("/{id}")
    public String viewReview(@PathVariable Long id, Model model) {
        Review review = reviewService.getReviewById(id);
        if (review == null) {
            return "redirect:/admin/reviews";
        }
        
        model.addAttribute("review", review);
        return "admin/review/review-detail";
    }

    @PostMapping("/{id}/delete")
    public String deleteReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reviewService.deleteReviewByAdmin(id);
            redirectAttributes.addFlashAttribute("success", "Xóa đánh giá thành công!");
        } catch (Exception e) {
            log.error("Lỗi khi xóa review: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/reviews";
    }

}
