package fsa.training.travelee.repository;

import fsa.training.travelee.entity.promotion.Promotion;
import fsa.training.travelee.entity.promotion.PromotionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    
    Optional<Promotion> findByCode(String code);
    
    boolean existsByCode(String code);
    
    @Query("SELECT p FROM Promotion p WHERE " +
           "(:keyword IS NULL OR LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:status IS NULL OR p.status = :status)")
    Page<Promotion> findByKeywordAndStatus(@Param("keyword") String keyword, 
                                          @Param("status") PromotionStatus status, 
                                          Pageable pageable);
    
    @Query("SELECT p FROM Promotion p WHERE p.endDate < :currentDate")
    List<Promotion> findExpiredPromotions(@Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT p FROM Promotion p WHERE p.startDate <= :currentDate AND p.endDate >= :currentDate AND p.status = 'ACTIVE'")
    List<Promotion> findActivePromotions(@Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT p FROM Promotion p WHERE p.usedCount >= p.totalUsageLimit")
    List<Promotion> findFullyUsedPromotions();
}
