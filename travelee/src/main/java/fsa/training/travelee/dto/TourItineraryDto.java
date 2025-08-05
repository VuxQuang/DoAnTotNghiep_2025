package fsa.training.travelee.dto;

import lombok.Data;

@Data
public class TourItineraryDto {
    private Integer dayNumber;
    private String title;
    private String description;
    private String activities;
    private String meals;
    private String accommodation;
}
