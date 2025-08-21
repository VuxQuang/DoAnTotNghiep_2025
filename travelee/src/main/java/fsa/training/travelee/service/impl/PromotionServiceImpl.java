package fsa.training.travelee.service.impl;

import fsa.training.travelee.dto.promotion.CreatePromotionDto;
import fsa.training.travelee.dto.promotion.PromotionDto;
import fsa.training.travelee.dto.promotion.UpdatePromotionDto;
import fsa.training.travelee.entity.Tour;
import fsa.training.travelee.entity.promotion.Promotion;
import fsa.training.travelee.entity.promotion.PromotionStatus;
import fsa.training.travelee.mapper.PromotionMapper;
import fsa.training.travelee.repository.PromotionRepository;
import fsa.training.travelee.repository.TourRepository;
import fsa.training.travelee.service.PromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PromotionServiceImpl implements PromotionService {
    
    private final PromotionRepository promotionRepository;
    private final TourRepository tourRepository;
    private final PromotionMapper promotionMapper;
    
    @Override
    public Page<PromotionDto> getAllPromotions(String keyword, PromotionStatus status, Pageable pageable) {
        log.info("Tìm kiếm promotions với keyword: {}, status: {}, page: {}", keyword, status, pageable.getPageNumber());
        
        Page<Promotion> promotions = promotionRepository.findByKeywordAndStatus(keyword, status, pageable);
        
        return promotions.map(promotionMapper::toDto);
    }
    
    @Override
    public PromotionDto getPromotionById(Long id) {
        log.info("Lấy promotion theo ID: {}", id);
        
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy promotion với ID: " + id));
        
        return promotionMapper.toDto(promotion);
    }
    
    @Override
    public PromotionDto getPromotionByCode(String code) {
        log.info("Lấy promotion theo code: {}", code);
        
        Promotion promotion = promotionRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy promotion với code: " + code));
        
        return promotionMapper.toDto(promotion);
    }
    
    @Override
    public PromotionDto createPromotion(CreatePromotionDto createPromotionDto) {
        log.info("Tạo mới promotion với code: {}", createPromotionDto.getCode());
        
        // Kiểm tra code đã tồn tại
        if (promotionRepository.existsByCode(createPromotionDto.getCode())) {
            throw new RuntimeException("Mã giảm giá đã tồn tại: " + createPromotionDto.getCode());
        }
        
        // Kiểm tra ngày bắt đầu và kết thúc
        if (createPromotionDto.getStartDate().isAfter(createPromotionDto.getEndDate())) {
            throw new RuntimeException("Ngày bắt đầu phải trước ngày kết thúc");
        }
        
        // Lấy danh sách tour áp dụng
        List<Tour> applicableTours = null;
        if (createPromotionDto.getApplicableTourIds() != null && !createPromotionDto.getApplicableTourIds().isEmpty()) {
            applicableTours = tourRepository.findAllById(createPromotionDto.getApplicableTourIds());
        }
        
        Promotion promotion = Promotion.builder()
                .code(createPromotionDto.getCode())
                .title(createPromotionDto.getTitle())
                .description(createPromotionDto.getDescription())
                .discountType(createPromotionDto.getDiscountType())
                .discountValue(createPromotionDto.getDiscountValue())
                .maxDiscountAmount(createPromotionDto.getMaxDiscountAmount())
                .minOrderAmount(createPromotionDto.getMinOrderAmount())
                .totalUsageLimit(createPromotionDto.getTotalUsageLimit())
                .usedCount(0)
                .userUsageLimit(createPromotionDto.getUserUsageLimit())
                .startDate(createPromotionDto.getStartDate())
                .endDate(createPromotionDto.getEndDate())
                .status(createPromotionDto.getStatus())
                .applicableTours(applicableTours)
                .build();
        
        Promotion savedPromotion = promotionRepository.save(promotion);
        log.info("Đã tạo promotion thành công với ID: {}", savedPromotion.getId());
        
        return promotionMapper.toDto(savedPromotion);
    }
    
    @Override
    public PromotionDto updatePromotion(Long id, UpdatePromotionDto updatePromotionDto) {
        log.info("Cập nhật promotion với ID: {}", id);
        
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy promotion với ID: " + id));
        
        // Kiểm tra ngày bắt đầu và kết thúc
        if (updatePromotionDto.getStartDate().isAfter(updatePromotionDto.getEndDate())) {
            throw new RuntimeException("Ngày bắt đầu phải trước ngày kết thúc");
        }
        
        // Lấy danh sách tour áp dụng
        List<Tour> applicableTours = null;
        if (updatePromotionDto.getApplicableTourIds() != null && !updatePromotionDto.getApplicableTourIds().isEmpty()) {
            applicableTours = tourRepository.findAllById(updatePromotionDto.getApplicableTourIds());
        }
        
        promotion.setTitle(updatePromotionDto.getTitle());
        promotion.setDescription(updatePromotionDto.getDescription());
        promotion.setDiscountType(updatePromotionDto.getDiscountType());
        promotion.setDiscountValue(updatePromotionDto.getDiscountValue());
        promotion.setMaxDiscountAmount(updatePromotionDto.getMaxDiscountAmount());
        promotion.setMinOrderAmount(updatePromotionDto.getMinOrderAmount());
        promotion.setTotalUsageLimit(updatePromotionDto.getTotalUsageLimit());
        promotion.setUserUsageLimit(updatePromotionDto.getUserUsageLimit());
        promotion.setStartDate(updatePromotionDto.getStartDate());
        promotion.setEndDate(updatePromotionDto.getEndDate());
        promotion.setStatus(updatePromotionDto.getStatus());
        promotion.setApplicableTours(applicableTours);
        
        Promotion updatedPromotion = promotionRepository.save(promotion);
        log.info("Đã cập nhật promotion thành công với ID: {}", updatedPromotion.getId());
        
        return promotionMapper.toDto(updatedPromotion);
    }
    
    @Override
    public void deletePromotion(Long id) {
        log.info("Xóa promotion với ID: {}", id);
        
        if (!promotionRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy promotion với ID: " + id);
        }
        
        promotionRepository.deleteById(id);
        log.info("Đã xóa promotion thành công với ID: {}", id);
    }
    
    @Override
    public void updatePromotionStatus(Long id, PromotionStatus status) {
        log.info("Cập nhật status promotion với ID: {} thành: {}", id, status);
        
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy promotion với ID: " + id));
        
        promotion.setStatus(status);
        promotionRepository.save(promotion);
        log.info("Đã cập nhật status promotion thành công");
    }
    
    @Override
    public List<PromotionDto> getExpiredPromotions() {
        log.info("Lấy danh sách promotions đã hết hạn");
        
        List<Promotion> expiredPromotions = promotionRepository.findExpiredPromotions(LocalDateTime.now());
        
        return expiredPromotions.stream()
                .map(promotionMapper::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<PromotionDto> getActivePromotions() {
        log.info("Lấy danh sách promotions đang hoạt động");
        
        List<Promotion> activePromotions = promotionRepository.findActivePromotions(LocalDateTime.now());
        
        return activePromotions.stream()
                .map(promotionMapper::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<PromotionDto> getFullyUsedPromotions() {
        log.info("Lấy danh sách promotions đã sử dụng hết");
        
        List<Promotion> fullyUsedPromotions = promotionRepository.findFullyUsedPromotions();
        
        return fullyUsedPromotions.stream()
                .map(promotionMapper::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public void updateExpiredPromotions() {
        log.info("Cập nhật trạng thái các promotions đã hết hạn");
        
        List<Promotion> expiredPromotions = promotionRepository.findExpiredPromotions(LocalDateTime.now());
        
        expiredPromotions.stream()
                .filter(promotion -> promotion.getStatus() != PromotionStatus.EXPIRED)
                .forEach(promotion -> {
                    promotion.setStatus(PromotionStatus.EXPIRED);
                    promotionRepository.save(promotion);
                });
        
        log.info("Đã cập nhật {} promotions hết hạn", expiredPromotions.size());
    }
    
    @Override
    public boolean isPromotionValid(String code) {
        log.info("Kiểm tra tính hợp lệ của promotion code: {}", code);
        
        return promotionRepository.findByCode(code)
                .map(promotion -> {
                    LocalDateTime now = LocalDateTime.now();
                    boolean isValid = promotion.getStatus() == PromotionStatus.ACTIVE &&
                            !now.isBefore(promotion.getStartDate()) &&
                            !now.isAfter(promotion.getEndDate()) &&
                            promotion.getUsedCount() < promotion.getTotalUsageLimit();
                    
                    log.info("Promotion code {} có hợp lệ: {}", code, isValid);
                    return isValid;
                })
                .orElse(false);
    }

    @Override
    public fsa.training.travelee.dto.promotion.PromotionValidationResponse validatePromotionCode(String code, Long tourId, BigDecimal totalAmount) {
        var builder = fsa.training.travelee.dto.promotion.PromotionValidationResponse.builder();
        try {
            Promotion promotion = promotionRepository.findByCode(code)
                    .orElse(null);
            if (promotion == null) {
                return builder.success(false).message("Mã giảm giá không tồn tại").build();
            }

            LocalDateTime now = LocalDateTime.now();
            boolean timeValid = !now.isBefore(promotion.getStartDate()) && !now.isAfter(promotion.getEndDate());
            boolean statusValid = promotion.getStatus() == PromotionStatus.ACTIVE;
            boolean usageValid = promotion.getUsedCount() < promotion.getTotalUsageLimit();

            if (!timeValid || !statusValid || !usageValid) {
                return builder.success(false).message("Mã giảm giá không còn hiệu lực").build();
            }

            // Kiểm tra tour áp dụng (nếu promotion có chỉ định danh sách tour)
            if (promotion.getApplicableTours() != null && !promotion.getApplicableTours().isEmpty()) {
                boolean applies = promotion.getApplicableTours().stream().anyMatch(t -> t.getId().equals(tourId));
                if (!applies) {
                    return builder.success(false).message("Mã giảm giá không áp dụng cho tour này").build();
                }
            }

            // Kiểm tra đơn tối thiểu
            if (promotion.getMinOrderAmount() != null && totalAmount != null) {
                if (totalAmount.compareTo(promotion.getMinOrderAmount()) < 0) {
                    return builder.success(false).message("Chưa đạt giá trị tối thiểu để áp dụng mã").build();
                }
            }

            // Tính số tiền giảm
            BigDecimal discountAmount;
            if (promotion.getDiscountType() == fsa.training.travelee.entity.promotion.DiscountType.PERCENTAGE) {
                discountAmount = totalAmount.multiply(promotion.getDiscountValue()).divide(new BigDecimal("100"));
            } else {
                discountAmount = promotion.getDiscountValue();
            }

            // Áp dụng mức giảm tối đa nếu có
            if (promotion.getMaxDiscountAmount() != null && promotion.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                discountAmount = discountAmount.min(promotion.getMaxDiscountAmount());
            }

            // Không cho âm
            if (discountAmount.compareTo(BigDecimal.ZERO) < 0) {
                discountAmount = BigDecimal.ZERO;
            }

            var info = fsa.training.travelee.dto.promotion.PromotionValidationResponse.PromotionInfo.builder()
                    .id(promotion.getId())
                    .code(promotion.getCode())
                    .title(promotion.getTitle())
                    .discountType(promotion.getDiscountType())
                    .discountValue(promotion.getDiscountValue())
                    .maxDiscountAmount(promotion.getMaxDiscountAmount())
                    .minOrderAmount(promotion.getMinOrderAmount())
                    .build();

            return builder
                    .success(true)
                    .promotion(info)
                    .discountAmount(discountAmount)
                    .message("Áp dụng mã giảm giá thành công")
                    .build();

        } catch (Exception ex) {
            return builder.success(false).message("Lỗi xử lý mã giảm giá").build();
        }
    }
}
