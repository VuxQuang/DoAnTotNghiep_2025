package fsa.training.travelee.service;

import fsa.training.travelee.entity.Tour;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface TourSearchService {

    Page<Tour> searchToursWithSpecification(
            String keyword,
            String departure,
            String destination,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            LocalDate departureDateFrom,
            LocalDate departureDateTo,
            Pageable pageable
    );
}
