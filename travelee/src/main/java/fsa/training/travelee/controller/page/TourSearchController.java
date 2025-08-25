package fsa.training.travelee.controller.page;

import fsa.training.travelee.entity.Tour;
import fsa.training.travelee.service.TourSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Controller
@RequestMapping("/page/tours")
@RequiredArgsConstructor
public class TourSearchController {

    private final TourSearchService tourSearchService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Search tours với dynamic filter
     */
    @GetMapping("/search")
    public String searchTours(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String departure,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) String minPrice,
            @RequestParam(required = false) String maxPrice,
            @RequestParam(required = false) String departureDateFrom,
            @RequestParam(required = false) String departureDateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model
    ) {
        log.info("Searching tours with parameters: keyword={}, departure={}, destination={}, " +
                        "price={}-{}, date={}-{}, page={}, size={}",
                keyword, departure, destination, minPrice, maxPrice, departureDateFrom, departureDateTo,
                page, size);

        try {
            // Parse parameters
            BigDecimal minPriceBD = parseBigDecimal(minPrice);
            BigDecimal maxPriceBD = parseBigDecimal(maxPrice);
            LocalDate fromDate = parseLocalDate(departureDateFrom);
            LocalDate toDate = parseLocalDate(departureDateTo);

            // Create pageable
            Pageable pageable = PageRequest.of(page, size);

            // Search tours
            Page<Tour> toursPage = tourSearchService.searchToursWithSpecification(
                    keyword, departure, destination, minPriceBD, maxPriceBD,
                    fromDate, toDate, pageable
            );

            // Add to model
            model.addAttribute("tours", toursPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", toursPage.getTotalPages());
            model.addAttribute("totalElements", toursPage.getTotalElements());
            model.addAttribute("hasNext", toursPage.hasNext());
            model.addAttribute("hasPrevious", toursPage.hasPrevious());

            // Add search parameters back to model for form persistence
            // Sử dụng trực tiếp thay vì tạo wrapper không cần thiết
            model.addAttribute("keyword", keyword);
            model.addAttribute("departure", departure);
            model.addAttribute("destination", destination);
            model.addAttribute("minPrice", minPrice);
            model.addAttribute("maxPrice", maxPrice);
            model.addAttribute("departureDateFrom", departureDateFrom);
            model.addAttribute("departureDateTo", departureDateTo);


            log.info("Found {} tours on page {} of {}", toursPage.getContent().size(), page + 1, toursPage.getTotalPages());

        } catch (Exception e) {
            log.error("Error searching tours", e);
            model.addAttribute("error", "Có lỗi xảy ra khi tìm kiếm tours: " + e.getMessage());
        }

        return "page/tour/tour-list";
    }

    /**
     * Home search - tìm kiếm đơn giản từ home page
     */
    @GetMapping("/home-search")
    public String homeSearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String departureDate,
            @RequestParam(required = false) String minPrice,
            @RequestParam(required = false) String maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model
    ) {
        log.info("Home search with parameters: keyword={}, departureDate={}, price={}-{}, page={}, size={}",
                keyword, departureDate, minPrice, maxPrice, page, size);

        try {
            // Parse parameters
            BigDecimal minPriceBD = parseBigDecimal(minPrice);
            BigDecimal maxPriceBD = parseBigDecimal(maxPrice);
            LocalDate departureDateLD = parseLocalDate(departureDate);

            // Create pageable
            Pageable pageable = PageRequest.of(page, size);

            // Search tours với Specification API (thay vì basic)
            Page<Tour> toursPage = tourSearchService.searchToursWithSpecification(
                    keyword, null, null, minPriceBD, maxPriceBD,
                    departureDateLD, departureDateLD, pageable
            );

            // Add to model
            model.addAttribute("tours", toursPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", toursPage.getTotalPages());
            model.addAttribute("totalElements", toursPage.getTotalElements());

            log.info("Home search found {} tours", toursPage.getContent().size());

        } catch (Exception e) {
            log.error("Error in home search", e);
            model.addAttribute("error", "Có lỗi xảy ra khi tìm kiếm: " + e.getMessage());
        }

        return "page/tour/tour-list";
    }

    /**
     * Parse BigDecimal từ String
     */
    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            log.warn("Invalid BigDecimal value: {}", value);
            return null;
        }
    }

    /**
     * Parse LocalDate từ String
     */
    private LocalDate parseLocalDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim(), DATE_FORMATTER);
        } catch (Exception e) {
            log.warn("Invalid date value: {}", value);
            return null;
        }
    }
}
