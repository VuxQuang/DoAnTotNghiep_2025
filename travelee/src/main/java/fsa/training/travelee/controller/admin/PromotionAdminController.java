package fsa.training.travelee.controller.admin;

import fsa.training.travelee.dto.promotion.CreatePromotionDto;
import fsa.training.travelee.dto.promotion.PromotionDto;
import fsa.training.travelee.dto.promotion.UpdatePromotionDto;
import fsa.training.travelee.dto.TourSelectionDto;
import fsa.training.travelee.entity.promotion.PromotionStatus;
import fsa.training.travelee.service.PromotionService;
import fsa.training.travelee.service.TourService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/promotions")
@RequiredArgsConstructor
@Slf4j
public class PromotionAdminController {

    private final PromotionService promotionService;
    private final TourService tourService;
    
    @GetMapping
    public String listPromotions(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) PromotionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Model model) {
        
        log.info("Hiển thị danh sách promotions - keyword: {}, status: {}, page: {}, size: {}", 
                keyword, status, page, size);
        
        // Tạo Pageable với sắp xếp
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Lấy danh sách promotions với phân trang
        Page<PromotionDto> promotionsPage = promotionService.getAllPromotions(keyword, status, pageable);
        
        // Lấy thống kê
        List<PromotionDto> expiredPromotions = promotionService.getExpiredPromotions();
        List<PromotionDto> activePromotions = promotionService.getActivePromotions();
        List<PromotionDto> fullyUsedPromotions = promotionService.getFullyUsedPromotions();
        
        // Tính toán thống kê sử dụng Stream API
        long totalPromotions = promotionsPage.getTotalElements();
        long activeCount = activePromotions.size();
        long expiredCount = expiredPromotions.size();
        long fullyUsedCount = fullyUsedPromotions.size();
        
        // Lấy danh sách status để hiển thị trong dropdown
        List<String> statusOptions = List.of("ACTIVE", "INACTIVE", "EXPIRED");
        
        model.addAttribute("promotions", promotionsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", promotionsPage.getTotalPages());
        model.addAttribute("totalElements", totalPromotions);
        model.addAttribute("size", size);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statusOptions", statusOptions);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        
        // Thống kê
        model.addAttribute("totalPromotions", totalPromotions);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("expiredCount", expiredCount);
        model.addAttribute("fullyUsedCount", fullyUsedCount);
        
        // Pagination info
        model.addAttribute("hasNext", promotionsPage.hasNext());
        model.addAttribute("hasPrevious", promotionsPage.hasPrevious());
        model.addAttribute("isFirst", promotionsPage.isFirst());
        model.addAttribute("isLast", promotionsPage.isLast());
        
        return "admin/promotion/promotion";
    }
    
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        log.info("Hiển thị form tạo mới promotion");

        try {
            // Lấy danh sách tất cả tour để admin chọn áp dụng
            log.info("=== CALLING tourService.getToursForSelection() ===");
            List<TourSelectionDto> allTours = tourService.getToursForSelection();
            
            // Debug logging
            log.info("=== DEBUG TOUR DATA ===");
            log.info("Total tours found: {}", allTours.size());
            if (!allTours.isEmpty()) {
                allTours.forEach(tour -> {
                    log.info("Tour ID: {}, Title: {}", 
                        tour.getId(), 
                        tour.getTitle());
                });
            } else {
                log.warn("Không tìm thấy tour nào trong database!");
                log.warn("Có thể database chưa có tour nào hoặc có lỗi khi lấy dữ liệu");
            }
            log.info("======================");
            
            model.addAttribute("allTours", allTours);
            
        } catch (Exception e) {
            log.error("LỖI khi lấy danh sách tours: {}", e.getMessage(), e);
            model.addAttribute("allTours", new ArrayList<>());
        }
        
        model.addAttribute("promotion", new CreatePromotionDto());
        model.addAttribute("discountTypes", List.of("PERCENTAGE", "FIXED_AMOUNT"));
        model.addAttribute("statusOptions", List.of("ACTIVE", "INACTIVE"));

        return "admin/promotion/create-promotion";
    }
    
    @PostMapping("/create")
    public String createPromotion(
            @Valid @ModelAttribute("promotion") CreatePromotionDto createPromotionDto,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        log.info("Tạo mới promotion với code: {}", createPromotionDto.getCode());
        
        if (result.hasErrors()) {
            log.warn("Có lỗi validation khi tạo promotion: {}", result.getAllErrors());
            model.addAttribute("discountTypes", List.of("PERCENTAGE", "FIXED_AMOUNT"));
            model.addAttribute("statusOptions", List.of("ACTIVE", "INACTIVE"));
            return "admin/promotion/create-promotion";
        }
        
        try {
            PromotionDto createdPromotion = promotionService.createPromotion(createPromotionDto);
            redirectAttributes.addFlashAttribute("success", 
                    "Tạo mã giảm giá '" + createdPromotion.getCode() + "' thành công!");
            log.info("Đã tạo promotion thành công với ID: {}", createdPromotion.getId());
            
            return "redirect:/admin/promotions";
            
        } catch (Exception e) {
            log.error("Lỗi khi tạo promotion: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tạo mã giảm giá: " + e.getMessage());
            model.addAttribute("discountTypes", List.of("PERCENTAGE", "FIXED_AMOUNT"));
            model.addAttribute("statusOptions", List.of("ACTIVE", "INACTIVE"));
            return "admin/promotion/create-promotion";
        }
    }
    
        @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        log.info("Hiển thị form chỉnh sửa promotion với ID: {}", id);

        try {
            PromotionDto promotion = promotionService.getPromotionById(id);
            UpdatePromotionDto updateDto = convertToUpdateDto(promotion);

            // Lấy danh sách tất cả tour để admin chọn áp dụng
            List<TourSelectionDto> allTours = tourService.getToursForSelection();
            
            // Debug logging
            log.info("=== DEBUG TOUR DATA (EDIT) ===");
            log.info("Total tours found: {}", allTours.size());
            if (!allTours.isEmpty()) {
                allTours.forEach(tour -> {
                    log.info("Tour ID: {}, Title: {}", 
                        tour.getId(), 
                        tour.getTitle());
                });
            } else {
                log.warn("Không tìm thấy tour nào trong database!");
            }
            log.info("=============================");

            model.addAttribute("promotion", updateDto);
            model.addAttribute("discountTypes", List.of("PERCENTAGE", "FIXED_AMOUNT"));
            model.addAttribute("statusOptions", List.of("ACTIVE", "INACTIVE", "EXPIRED"));
            model.addAttribute("allTours", allTours);

            return "admin/promotion/edit-promotion";

        } catch (Exception e) {
            log.error("Lỗi khi lấy promotion để chỉnh sửa: {}", e.getMessage());
            return "redirect:/admin/promotions";
        }
    }
    
    @PostMapping("/edit/{id}")
    public String updatePromotion(
            @PathVariable Long id,
            @Valid @ModelAttribute("promotion") UpdatePromotionDto updatePromotionDto,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        log.info("Cập nhật promotion với ID: {}", id);
        
        if (result.hasErrors()) {
            log.warn("Có lỗi validation khi cập nhật promotion: {}", result.getAllErrors());
            model.addAttribute("discountTypes", List.of("PERCENTAGE", "FIXED_AMOUNT"));
            model.addAttribute("statusOptions", List.of("ACTIVE", "INACTIVE", "EXPIRED"));
            return "admin/promotion/edit-promotion";
        }
        
        try {
            PromotionDto updatedPromotion = promotionService.updatePromotion(id, updatePromotionDto);
            redirectAttributes.addFlashAttribute("success", 
                    "Cập nhật mã giảm giá '" + updatedPromotion.getCode() + "' thành công!");
            log.info("Đã cập nhật promotion thành công với ID: {}", updatedPromotion.getId());
            
            return "redirect:/admin/promotions";
            
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật promotion: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật mã giảm giá: " + e.getMessage());
            model.addAttribute("discountTypes", List.of("PERCENTAGE", "FIXED_AMOUNT"));
            model.addAttribute("statusOptions", List.of("ACTIVE", "INACTIVE", "EXPIRED"));
            return "admin/promotion/edit-promotion";
        }
    }
    
    @PostMapping("/delete/{id}")
    public String deletePromotion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("Xóa promotion với ID: {}", id);
        
        try {
            promotionService.deletePromotion(id);
            redirectAttributes.addFlashAttribute("success", "Xóa mã giảm giá thành công!");
            log.info("Đã xóa promotion thành công với ID: {}", id);
            
        } catch (Exception e) {
            log.error("Lỗi khi xóa promotion: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa mã giảm giá: " + e.getMessage());
        }
        
        return "redirect:/admin/promotions";
    }
    
    @PostMapping("/{id}/status")
    @ResponseBody
    public ResponseEntity<String> updateStatus(
            @PathVariable Long id,
            @RequestParam PromotionStatus status) {
        
        log.info("Cập nhật status promotion với ID: {} thành: {}", id, status);
        
        try {
            promotionService.updatePromotionStatus(id, status);
            return ResponseEntity.ok("Cập nhật trạng thái thành công!");
            
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật status promotion: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
    
    @PostMapping("/update-expired")
    @ResponseBody
    public ResponseEntity<String> updateExpiredPromotions() {
        log.info("Cập nhật trạng thái các promotions đã hết hạn");
        
        try {
            promotionService.updateExpiredPromotions();
            return ResponseEntity.ok("Đã cập nhật các promotions hết hạn!");
            
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật promotions hết hạn: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
    
    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<Page<PromotionDto>> searchPromotions(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) PromotionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("API tìm kiếm promotions - keyword: {}, status: {}, page: {}, size: {}", 
                keyword, status, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<PromotionDto> promotions = promotionService.getAllPromotions(keyword, status, pageable);
        
        return ResponseEntity.ok(promotions);
    }
    
    private UpdatePromotionDto convertToUpdateDto(PromotionDto promotionDto) {
        return UpdatePromotionDto.builder()
                .id(promotionDto.getId())
                .title(promotionDto.getTitle())
                .description(promotionDto.getDescription())
                .discountType(promotionDto.getDiscountType())
                .discountValue(promotionDto.getDiscountValue())
                .maxDiscountAmount(promotionDto.getMaxDiscountAmount())
                .minOrderAmount(promotionDto.getMinOrderAmount())
                .totalUsageLimit(promotionDto.getTotalUsageLimit())
                .userUsageLimit(promotionDto.getUserUsageLimit())
                .startDate(promotionDto.getStartDate())
                .endDate(promotionDto.getEndDate())
                .status(promotionDto.getStatus())
                .applicableTourIds(promotionDto.getApplicableTourIds())
                .build();
    }
}
