package fsa.training.travelee.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TourScheduleDto {
    private LocalDate departureDate;
    private LocalDate returnDate;
    private BigDecimal specialPrice;
    private Integer availableSlots;
    private String status;
}
