package fsa.training.travelee.service.impl;

import fsa.training.travelee.entity.Tour;
import fsa.training.travelee.repository.TourRepository;
import fsa.training.travelee.service.TourSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import jakarta.persistence.criteria.JoinType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class TourSearchServiceImpl implements TourSearchService {

    private final TourRepository tourRepository;

    @Override
    public Page<Tour> searchToursWithSpecification(
            String keyword,
            String departure,
            String destination,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            LocalDate departureDateFrom,
            LocalDate departureDateTo,
            Pageable pageable
    ) {
        log.info("🔍 Searching tours with specification: keyword='{}', departure='{}', destination='{}', " +
                        "price={}-{}, date={}-{}, sort={} {}",
                keyword, departure, destination, minPrice, maxPrice, departureDateFrom, departureDateTo);

        // Tạo Specification
        Specification<Tour> spec = createTourSpecification(
                keyword, departure, destination, minPrice, maxPrice,
                departureDateFrom, departureDateTo
        );

        log.debug("Tour specification created: {}", spec);

        // Sử dụng Pageable trực tiếp (không có sort)
        log.debug("Pageable created: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        log.info("Executing search with specification...");
        Page<Tour> result = tourRepository.findAll(spec, pageable);
        log.info("Search completed. Found {} tours on page {} of {}",
                result.getContent().size(), result.getNumber() + 1, result.getTotalPages());

        // Log chi tiết về tours tìm được
        if (!result.getContent().isEmpty()) {
            result.getContent().forEach(tour ->
                    log.info("Tour found: ID={}, Title='{}', Status='{}', Departure='{}', Destination='{}'",
                            tour.getId(), tour.getTitle(), tour.getStatus(), tour.getDeparture(), tour.getDestination())
            );
        } else {
            log.warn("No tours found with current specification!");
        }

        return result;
    }

    private Specification<Tour> createTourSpecification(
            String keyword,
            String departure,
            String destination,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            LocalDate departureDateFrom,
            LocalDate departureDateTo
    ) {
        log.debug("🔧 Creating tour specification with: keyword='{}', departure='{}', destination='{}', " +
                        "price={}-{}, date={}-{}", keyword, departure, destination, minPrice, maxPrice,
                departureDateFrom, departureDateTo);

        Specification<Tour> spec = Specification.where(hasKeyword(keyword))
                .and(hasDeparture(departure))
                .and(hasDestination(destination))
                .and(hasPriceRange(minPrice, maxPrice))
                .and(hasDateRange(departureDateFrom, departureDateTo))
                .and(isActive());

        log.debug("Tour specification created successfully with {} conditions",
                (keyword != null ? 1 : 0) +
                        (departure != null ? 1 : 0) +
                        (destination != null ? 1 : 0) +
                        ((minPrice != null || maxPrice != null) ? 1 : 0) +
                        ((departureDateFrom != null || departureDateTo != null) ? 1 : 0) + 1); // +1 for isActive

        return spec;
    }

    private Specification<Tour> hasKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(keyword)) {
                log.debug("Keyword is empty, returning conjunction");
                return criteriaBuilder.conjunction();
            }

            String likePattern = "%" + keyword.toLowerCase() + "%";
            log.debug("Creating keyword search with pattern: '{}' for fields: title, destination", likePattern);

            var result = criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("destination")), likePattern)
            );

            log.debug("Keyword search specification created successfully for keyword: '{}'", keyword);
            return result;
        };
    }

    private Specification<Tour> hasDeparture(String departure) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(departure)) {
                log.debug("Departure is empty, returning conjunction");
                return criteriaBuilder.conjunction();
            }
            log.debug("Filtering by departure: '{}'", departure);
            return criteriaBuilder.equal(root.get("departure"), departure);
        };
    }

    private Specification<Tour> hasDestination(String destination) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(destination)) {
                log.debug("Destination is empty, returning conjunction");
                return criteriaBuilder.conjunction();
            }
            log.debug("Filtering by destination: '{}'", destination);
            return criteriaBuilder.equal(root.get("destination"), destination);
        };
    }

    private Specification<Tour> hasPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> {
            if (minPrice == null && maxPrice == null) {
                return criteriaBuilder.conjunction();
            }

            if (minPrice != null && maxPrice != null) {
                return criteriaBuilder.between(root.get("adultPrice"), minPrice, maxPrice);
            } else if (minPrice != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("adultPrice"), minPrice);
            } else {
                return criteriaBuilder.lessThanOrEqualTo(root.get("adultPrice"), maxPrice);
            }
        };
    }

    /**
     * Kiểm tra date range
     */
    private Specification<Tour> hasDateRange(LocalDate departureDateFrom, LocalDate departureDateTo) {
        return (root, query, criteriaBuilder) -> {
            if (departureDateFrom == null && departureDateTo == null) {
                return criteriaBuilder.conjunction();
            }

            // Sử dụng LEFT JOIN để không mất tours không có schedule
            var scheduleJoin = root.join("schedules", jakarta.persistence.criteria.JoinType.LEFT);

            if (departureDateFrom != null && departureDateTo != null) {
                return criteriaBuilder.between(scheduleJoin.get("departureDate"), departureDateFrom, departureDateTo);
            }

            if (departureDateFrom != null) {
                return criteriaBuilder.greaterThanOrEqualTo(scheduleJoin.get("departureDate"), departureDateFrom);
            }

            return criteriaBuilder.lessThanOrEqualTo(scheduleJoin.get("departureDate"), departureDateTo);
        };
    }

    private Specification<Tour> isActive() {
        return (root, query, criteriaBuilder) -> {
            log.debug("Checking tour status = 'active' (case-insensitive)");
            return criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get("status")),
                    "active"
            );
        };
    }


}
