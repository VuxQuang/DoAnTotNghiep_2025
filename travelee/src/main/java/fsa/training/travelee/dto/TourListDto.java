package fsa.training.travelee.dto;

import lombok.Data;

import java.util.List;

@Data
public class TourListDto {
    private Long id;
    private String title;
    private String categoryName;
    private String status;
    private List<String> imageUrls;
}
