package fsa.training.travelee.dto.promotion;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PromotionValidationRequest {
    private String code;
    private Long tourId;
    private BigDecimal totalAmount;
}
