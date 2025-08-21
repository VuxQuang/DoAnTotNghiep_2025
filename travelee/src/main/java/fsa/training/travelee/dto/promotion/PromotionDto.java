package fsa.training.travelee.dto.promotion;

import fsa.training.travelee.entity.promotion.DiscountType;
import fsa.training.travelee.entity.promotion.PromotionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionDto {
    private Long id;
    private String code;
    private String title;
    private String description;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscountAmount;
    private BigDecimal minOrderAmount;
    private int totalUsageLimit;
    private int usedCount;
    private int userUsageLimit;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private PromotionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Long> applicableTourIds;
    private String statusDisplay;
    private String discountDisplay;
    private boolean isExpired;
    private boolean isFullyUsed;
}
