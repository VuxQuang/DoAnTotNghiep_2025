package fsa.training.travelee.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class TourCreateRequest {

    private String title;
    private Long categoryId;
    private String departure;
    private String destination;
    private Integer duration;
    private Integer maxParticipants;
    private String description;
    private String highlights;
    private BigDecimal adultPrice;
    private BigDecimal childPrice;
    private String status;
    private Boolean featured;
    private Boolean isHot;
    private Boolean hasPromotion;
    private List<String> includes;
    private List<String> excludes;
    private String terms;
    private List<String> imageUrls;
    private List<TourItineraryDto> itineraries;
    private List<TourScheduleDto> schedules;
}
