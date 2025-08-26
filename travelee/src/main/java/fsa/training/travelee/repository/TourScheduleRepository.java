package fsa.training.travelee.repository;

import fsa.training.travelee.entity.TourSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TourScheduleRepository extends JpaRepository<TourSchedule, Long> {
    @Query("SELECT s FROM TourSchedule s JOIN FETCH s.tour t WHERE s.departureDate BETWEEN :start AND :end")
    List<TourSchedule> findByDepartureDateBetweenWithTour(@Param("start") LocalDate start,
                                                          @Param("end") LocalDate end);
}
