package fsa.training.travelee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyBookingStatsDto {
    private LocalDate date;
    private long bookingCount;
    private BigDecimal revenue;
}
