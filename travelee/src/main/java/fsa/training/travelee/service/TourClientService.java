package fsa.training.travelee.service;

import fsa.training.travelee.dto.TourListClientDto;
import org.springframework.data.domain.Page;

public interface TourClientService {
    Page<TourListClientDto> getNewestTours(int page, int size);



}
