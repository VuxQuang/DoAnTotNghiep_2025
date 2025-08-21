package fsa.training.travelee.dto.promotion;

import fsa.training.travelee.entity.promotion.DiscountType;
import fsa.training.travelee.entity.promotion.PromotionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePromotionDto {
    
    @NotBlank(message = "Mã giảm giá không được để trống")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Mã giảm giá chỉ được chứa chữ hoa và số")
    private String code;
    
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;
    
    private String description;
    
    @NotNull(message = "Loại giảm giá không được để trống")
    private DiscountType discountType;
    
    @NotNull(message = "Giá trị giảm giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá trị giảm giá phải lớn hơn 0")
    private BigDecimal discountValue;
    
    @DecimalMin(value = "0.0", message = "Giá trị giảm tối đa không được âm")
    private BigDecimal maxDiscountAmount;
    
    @DecimalMin(value = "0.0", message = "Giá trị đơn hàng tối thiểu không được âm")
    private BigDecimal minOrderAmount;
    
    @Min(value = 1, message = "Giới hạn sử dụng tổng phải lớn hơn 0")
    private int totalUsageLimit;
    
    @Min(value = 1, message = "Giới hạn sử dụng mỗi user phải lớn hơn 0")
    private int userUsageLimit;
    
    @NotNull(message = "Ngày bắt đầu không được để trống")
    @Future(message = "Ngày bắt đầu phải trong tương lai")
    private LocalDateTime startDate;
    
    @NotNull(message = "Ngày kết thúc không được để trống")
    @Future(message = "Ngày kết thúc phải trong tương lai")
    private LocalDateTime endDate;
    
    @NotNull(message = "Trạng thái không được để trống")
    private PromotionStatus status;
    
    private List<Long> applicableTourIds;
}
