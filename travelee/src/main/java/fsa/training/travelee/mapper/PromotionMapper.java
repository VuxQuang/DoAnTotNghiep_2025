package fsa.training.travelee.mapper;

import fsa.training.travelee.dto.promotion.PromotionDto;
import fsa.training.travelee.entity.promotion.Promotion;
import fsa.training.travelee.entity.promotion.PromotionStatus;
import fsa.training.travelee.entity.promotion.DiscountType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PromotionMapper {
    
    public PromotionDto toDto(Promotion promotion) {
        if (promotion == null) {
            return null;
        }
        
        LocalDateTime now = LocalDateTime.now();
        boolean isExpired = promotion.getEndDate().isBefore(now);
        boolean isFullyUsed = promotion.getUsedCount() >= promotion.getTotalUsageLimit();
        
        String statusDisplay = getStatusDisplay(promotion.getStatus(), isExpired, isFullyUsed);
        String discountDisplay = getDiscountDisplay(promotion.getDiscountType(), promotion.getDiscountValue());
        
        List<Long> tourIds = promotion.getApplicableTours() != null ? 
            promotion.getApplicableTours().stream()
                .map(tour -> tour.getId())
                .collect(Collectors.toList()) : 
            List.of();
        
        return PromotionDto.builder()
                .id(promotion.getId())
                .code(promotion.getCode())
                .title(promotion.getTitle())
                .description(promotion.getDescription())
                .discountType(promotion.getDiscountType())
                .discountValue(promotion.getDiscountValue())
                .maxDiscountAmount(promotion.getMaxDiscountAmount())
                .minOrderAmount(promotion.getMinOrderAmount())
                .totalUsageLimit(promotion.getTotalUsageLimit())
                .usedCount(promotion.getUsedCount())
                .userUsageLimit(promotion.getUserUsageLimit())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .status(promotion.getStatus())
                .createdAt(promotion.getCreatedAt())
                .updatedAt(promotion.getUpdatedAt())
                .applicableTourIds(tourIds)
                .statusDisplay(statusDisplay)
                .discountDisplay(discountDisplay)
                .isExpired(isExpired)
                .isFullyUsed(isFullyUsed)
                .build();
    }
    
    public List<PromotionDto> toDtoList(List<Promotion> promotions) {
        if (promotions == null) {
            return List.of();
        }
        
        return promotions.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    private String getStatusDisplay(PromotionStatus status, boolean isExpired, boolean isFullyUsed) {
        if (isExpired) {
            return "Hết hạn";
        }
        if (isFullyUsed) {
            return "Đã sử dụng hết";
        }
        
        return switch (status) {
            case ACTIVE -> "Đang hoạt động";
            case INACTIVE -> "Không hoạt động";
            case EXPIRED -> "Hết hạn";
        };
    }
    
    private String getDiscountDisplay(DiscountType discountType, java.math.BigDecimal discountValue) {
        return switch (discountType) {
            case PERCENTAGE -> discountValue + "%";
            case FIXED_AMOUNT -> discountValue + " VNĐ";
        };
    }
}
