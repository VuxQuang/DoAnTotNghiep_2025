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
                    .sorted((i1, i2) -> Integer.compare(i1.getSortOrder(), i2.getSortOrder()))
                    .map(TourImage::getImageUrl)
                    .toList()
                    : List.of()
            );

            return dto;
        }).toList();
    }


    @Override
    public void deleteTourById(Long id) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tour"));
        tourRepository.delete(tour);
    }

    @Override
    public void updateTour(Long id, TourCreateRequest request) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tour"));
        
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));

        // Cập nhật thông tin cơ bản
        tour.setTitle(request.getTitle());
        tour.setCategory(category);
        tour.setDeparture(request.getDeparture());
        tour.setDescription(request.getDescription());
        tour.setDestination(request.getDestination());
        tour.setDuration(request.getDuration());
        tour.setHighlights(request.getHighlights());
        tour.setAdultPrice(request.getAdultPrice());
        tour.setChildPrice(request.getChildPrice());
        tour.setMaxParticipants(request.getMaxParticipants());
        tour.setStatus(request.getStatus());
        tour.setFeatured(request.getFeatured());
        tour.setIsHot(request.getIsHot());
        tour.setHasPromotion(request.getHasPromotion());
        tour.setIncludes(convertListToString(request.getIncludes()));
        tour.setExcludes(convertListToString(request.getExcludes()));
        tour.setTerms(request.getTerms());

        // Xóa các dữ liệu cũ và thêm mới
        tour.getImages().clear();
        tour.getItineraries().clear();
        tour.getSchedules().clear();

        // Map lại các dữ liệu liên quan
        tourMapper.mapImages(request.getImageUrls(), tour);
        tourMapper.mapItineraries(request.getItineraries(), tour);
        tourMapper.mapSchedules(request.getSchedules(), tour);

        tourRepository.save(tour);
    }

    @Override
    public Tour getById(Long id) {
        // Sử dụng custom query để fetch tất cả related data
        Tour tour = tourRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tour"));
        
        // Debug logging
        System.out.println("=== DEBUG TOUR DATA ===");
        System.out.println("Tour ID: " + tour.getId());
        System.out.println("Tour Title: " + tour.getTitle());
        System.out.println("Images: " + (tour.getImages() != null ? tour.getImages().size() : "null"));
        System.out.println("Itineraries: " + (tour.getItineraries() != null ? tour.getItineraries().size() : "null"));
        System.out.println("Schedules: " + (tour.getSchedules() != null ? tour.getSchedules().size() : "null"));
        System.out.println("Category: " + (tour.getCategory() != null ? tour.getCategory().getName() : "null"));
        
        if (tour.getItineraries() != null && !tour.getItineraries().isEmpty()) {
            tour.getItineraries().forEach(it -> 
                System.out.println("  Itinerary: Day " + it.getDayNumber() + " - " + it.getTitle())
            );
        }
        
        if (tour.getSchedules() != null && !tour.getSchedules().isEmpty()) {
            tour.getSchedules().forEach(sch -> 
                System.out.println("  Schedule: " + sch.getDepartureDate() + " - " + sch.getStatus())
            );
        }
        System.out.println("======================");
        
        return tour;
    }

    private String convertListToString(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String item : list) {
            if (item != null && !item.trim().isEmpty()) {
                sb.append("<p>").append(item.trim()).append("</p>");
            }
        }
        return sb.toString();
    }
}
