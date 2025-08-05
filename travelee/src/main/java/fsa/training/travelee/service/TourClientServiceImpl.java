package fsa.training.travelee.service;

import fsa.training.travelee.dto.TourListClientDto;
import fsa.training.travelee.dto.TourListDto;
import fsa.training.travelee.entity.Tour;
import fsa.training.travelee.entity.TourImage;
import fsa.training.travelee.repository.TourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TourClientServiceImpl implements TourClientService{

    private final TourRepository tourRepository;


    @Override
    public Page<TourListClientDto> getNewestTours(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Tour> tourPage = tourRepository.findAllByStatus("ACTIVE", pageable);

        return tourPage.map(tour -> {
            TourListClientDto dto = new TourListClientDto();
            dto.setId(tour.getId());
            dto.setTitle(tour.getTitle());
            dto.setDeparture(tour.getDeparture());
            dto.setDestination(tour.getDestination());
            dto.setAdultPrice(tour.getAdultPrice());
            dto.setImageUrls(tour.getImages() != null
                    ? tour.getImages().stream().map(TourImage::getImageUrl).toList()
                    : List.of());
            return dto;
        });
    }

}
