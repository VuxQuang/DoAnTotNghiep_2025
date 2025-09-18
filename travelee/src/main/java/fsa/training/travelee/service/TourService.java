package fsa.training.travelee.service;

import fsa.training.travelee.dto.TourCreateRequest;
import fsa.training.travelee.dto.TourListDto;
import fsa.training.travelee.dto.TourSelectionDto;
import fsa.training.travelee.entity.Tour;
import org.springframework.data.domain.Page;


import java.util.List;

public interface TourService {
    void createTour(TourCreateRequest request);

    List<TourListDto> getAllTours();
    List<TourSelectionDto> getToursForSelection();
    void deleteTourById(Long id);
    void updateTour(Long id, TourCreateRequest request);
    Tour getById(Long id);
    long countTours();
}
