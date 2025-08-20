package fsa.training.travelee.dto;

import lombok.Data;
import java.util.List;

@Data
public class TourItineraryDto {
    private Integer dayNumber;
    private String title;
    private List<String> description;
    private List<String> activities;
    private String meals;
    private String accommodation;
}
