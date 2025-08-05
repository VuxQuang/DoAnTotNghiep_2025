package fsa.training.travelee.service;

import fsa.training.travelee.dto.TourCreateRequest;
import fsa.training.travelee.dto.TourListDto;
import org.springframework.data.domain.Page;


import java.util.List;

public interface TourService {
    void createTour(TourCreateRequest request);

    List<TourListDto> getAllTours();
    void deleteTourById(Long id);


}
