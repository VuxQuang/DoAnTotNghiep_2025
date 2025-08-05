package fsa.training.travelee.service;

import fsa.training.travelee.dto.TourCreateRequest;
import fsa.training.travelee.dto.TourListDto;
import fsa.training.travelee.entity.*;
import fsa.training.travelee.mapper.TourMapper;
import fsa.training.travelee.repository.CategoryRepository;
import fsa.training.travelee.repository.TourRepository;
import fsa.training.travelee.service.TourService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TourServiceImpl implements TourService {

    private final TourRepository tourRepository;
    private final CategoryRepository categoryRepository;
    private final TourMapper tourMapper;

    @Override
    public void createTour(TourCreateRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));

        Tour tour = tourMapper.toEntity(request, category);
        tourMapper.mapImages(request.getImageUrls(), tour);
        tourMapper.mapItineraries(request.getItineraries(), tour);
        tourMapper.mapSchedules(request.getSchedules(), tour);

        tourRepository.save(tour);
    }

    @Override
    public List<TourListDto> getAllTours() {
        return tourRepository.findAll().stream().map(tour -> {
            TourListDto dto = new TourListDto();
            dto.setId(tour.getId());
            dto.setTitle(tour.getTitle());
            dto.setStatus(tour.getStatus());

            if (tour.getCategory() != null) {
                dto.setCategoryName(tour.getCategory().getName());
            }

            dto.setImageUrls(tour.getImages() != null
                    ? tour.getImages().stream()
                    .map(TourImage::getImageUrl)
                    .toList()
                    : List.of()
            );

            return dto;
        }).toList();
    }


    @Override
    public void deleteTourById(Long id) {

    }


}
