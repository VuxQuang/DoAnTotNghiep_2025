package fsa.training.travelee.service;

import fsa.training.travelee.dto.promotion.CreatePromotionDto;
import fsa.training.travelee.dto.promotion.PromotionDto;
import fsa.training.travelee.dto.promotion.UpdatePromotionDto;
import fsa.training.travelee.dto.promotion.PromotionValidationResponse;
import fsa.training.travelee.entity.promotion.PromotionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.math.BigDecimal;

public interface PromotionService {
    
    Page<PromotionDto> getAllPromotions(String keyword, PromotionStatus status, Pageable pageable);
    
    PromotionDto getPromotionById(Long id);
    
    PromotionDto getPromotionByCode(String code);
    
    PromotionDto createPromotion(CreatePromotionDto createPromotionDto);
    
    PromotionDto updatePromotion(Long id, UpdatePromotionDto updatePromotionDto);
    
    void deletePromotion(Long id);
    
    void updatePromotionStatus(Long id, PromotionStatus status);
    
    List<PromotionDto> getExpiredPromotions();
    
    List<PromotionDto> getActivePromotions();
    
    List<PromotionDto> getFullyUsedPromotions();
    
    void updateExpiredPromotions();
    
    boolean isPromotionValid(String code);

    PromotionValidationResponse validatePromotionCode(String code, Long tourId, BigDecimal totalAmount);
}
