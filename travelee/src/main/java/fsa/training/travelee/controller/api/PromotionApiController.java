package fsa.training.travelee.controller.api;

import fsa.training.travelee.dto.promotion.PromotionValidationRequest;
import fsa.training.travelee.dto.promotion.PromotionValidationResponse;
import fsa.training.travelee.service.PromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@Slf4j
public class PromotionApiController {

    private final PromotionService promotionService;

    @PostMapping("/validate")
    public ResponseEntity<PromotionValidationResponse> validatePromotionCode(
            @RequestBody PromotionValidationRequest request) {
        
        log.info("Validating promotion code: {} for tour: {} with amount: {}", 
                request.getCode(), request.getTourId(), request.getTotalAmount());
        
        try {
            PromotionValidationResponse response = promotionService.validatePromotionCode(
                    request.getCode(), 
                    request.getTourId(), 
                    request.getTotalAmount()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error validating promotion code: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    PromotionValidationResponse.builder()
                            .success(false)
                            .message("Có lỗi xảy ra khi kiểm tra mã giảm giá")
                            .build()
            );
        }
    }
}
