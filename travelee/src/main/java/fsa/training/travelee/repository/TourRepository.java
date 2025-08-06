package fsa.training.travelee.repository;

import fsa.training.travelee.entity.Tour;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TourRepository extends JpaRepository<Tour, Long> {

    @Query("SELECT t FROM Tour t WHERE t.status = :status")
    Page<Tour> findAllByStatus(@Param("status") String status, Pageable pageable);

    @Query("SELECT t FROM Tour t WHERE t.id = :id AND t.status = :status")
    Optional<Tour> findByIdAndStatus(@Param("id") Long id, @Param("status") String status);

}
