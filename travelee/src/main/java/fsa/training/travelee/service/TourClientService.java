package fsa.training.travelee.service;

import fsa.training.travelee.dto.TourListClientDto;
import fsa.training.travelee.entity.Tour;
import org.springframework.data.domain.Page;

public interface TourClientService {
    Page<TourListClientDto> getNewestTours(int page, int size);
    
    Tour getTourById(Long id);
}
