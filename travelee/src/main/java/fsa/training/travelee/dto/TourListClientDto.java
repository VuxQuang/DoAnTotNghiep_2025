package fsa.training.travelee.dto;

import lombok.Data;
import java.util.List;
import java.math.BigDecimal;

@Data
public class TourListClientDto {
    private Long id;
    private String title;
    private String departure;
    private String destination;
    private List<String> imageUrls;
    private BigDecimal adultPrice;
}
