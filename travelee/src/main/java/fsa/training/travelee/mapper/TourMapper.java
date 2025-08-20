package fsa.training.travelee.mapper;

import fsa.training.travelee.dto.TourCreateRequest;
import fsa.training.travelee.entity.*;
import org.springframework.stereotype.Component;
import fsa.training.travelee.dto.TourItineraryDto;
import fsa.training.travelee.dto.TourScheduleDto;

import java.util.List;

@Component
public class TourMapper {

    public Tour toEntity(TourCreateRequest request, Category category) {
        return Tour.builder()
                .title(request.getTitle())
                .category(category)
                .departure(request.getDeparture())
                .description(request.getDescription())
                .destination(request.getDestination())
                .duration(request.getDuration())
                .highlights(request.getHighlights())
                .adultPrice(request.getAdultPrice())
                .childPrice(request.getChildPrice())
                .maxParticipants(request.getMaxParticipants())
                .status(request.getStatus())
                .featured(request.getFeatured())
                .isHot(request.getIsHot())
                .hasPromotion(request.getHasPromotion())
                .includes(convertListToString(request.getIncludes()))
                .excludes(convertListToString(request.getExcludes()))
                .terms(request.getTerms())
                .build();
    }

    public void mapImages(List<String> imageUrls, Tour tour) {
        if (imageUrls != null) {
            for (int i = 0; i < imageUrls.size(); i++) {
                TourImage image = TourImage.builder()
                        .imageUrl(imageUrls.get(i))
                        .isPrimary(i == 0)
                        .sortOrder(i)
                        .tour(tour)
                        .build();
                tour.getImages().add(image);
            }
        }
    }

    public void mapItineraries(List<TourItineraryDto> itineraries, Tour tour) {
        if (itineraries != null) {
            itineraries.forEach(itineraryDto -> {
                TourItinerary itinerary = TourItinerary.builder()
                        .dayNumber(itineraryDto.getDayNumber())
                        .title(itineraryDto.getTitle())
                        .description(convertListToString(itineraryDto.getDescription()))
                        .activities(convertListToString(itineraryDto.getActivities()))
                        .meals(itineraryDto.getMeals())
                        .accommodation(itineraryDto.getAccommodation())
                        .tour(tour)
                        .build();
                tour.getItineraries().add(itinerary);
            });
        }
    }

    public void mapSchedules(List<TourScheduleDto> schedules, Tour tour) {
        if (schedules != null) {
            schedules.forEach(scheduleDto -> {
                TourSchedule schedule = TourSchedule.builder()
                        .departureDate(scheduleDto.getDepartureDate())
                        .returnDate(scheduleDto.getReturnDate())
                        .specialPrice(scheduleDto.getSpecialPrice())
                        .availableSlots(scheduleDto.getAvailableSlots())
                        .status(scheduleDto.getStatus())
                        .tour(tour)
                        .build();
                tour.getSchedules().add(schedule);
            });
        }
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

    public TourCreateRequest toDto(Tour tour) {
        TourCreateRequest dto = new TourCreateRequest();
        dto.setTitle(tour.getTitle());
        dto.setCategoryId(tour.getCategory().getId());
        dto.setDeparture(tour.getDeparture());
        dto.setDescription(tour.getDescription());
        dto.setDestination(tour.getDestination());
        dto.setDuration(tour.getDuration());
        dto.setHighlights(tour.getHighlights());
        dto.setAdultPrice(tour.getAdultPrice());
        dto.setChildPrice(tour.getChildPrice());
        dto.setMaxParticipants(tour.getMaxParticipants());
        dto.setStatus(tour.getStatus());
        dto.setFeatured(tour.getFeatured());
        dto.setIsHot(tour.getIsHot());
        dto.setHasPromotion(tour.getHasPromotion());
        dto.setIncludes(convertStringToList(tour.getIncludes()));
        dto.setExcludes(convertStringToList(tour.getExcludes()));
        dto.setTerms(tour.getTerms());
        
        // Map images
        if (tour.getImages() != null) {
            dto.setImageUrls(tour.getImages().stream()
                    .sorted((i1, i2) -> Integer.compare(i1.getSortOrder(), i2.getSortOrder()))
                    .map(TourImage::getImageUrl)
                    .toList());
        }
        
        // Map itineraries
        if (tour.getItineraries() != null) {
            dto.setItineraries(tour.getItineraries().stream()
                    .sorted((i1, i2) -> Integer.compare(i1.getDayNumber(), i2.getDayNumber()))
                    .map(this::mapItineraryToDto)
                    .toList());
        }
        
        // Map schedules
        if (tour.getSchedules() != null) {
            dto.setSchedules(tour.getSchedules().stream()
                    .map(this::mapScheduleToDto)
                    .toList());
        }
        
        return dto;
    }

    private TourItineraryDto mapItineraryToDto(TourItinerary itinerary) {
        TourItineraryDto dto = new TourItineraryDto();
        dto.setDayNumber(itinerary.getDayNumber());
        dto.setTitle(itinerary.getTitle());
        dto.setDescription(convertStringToList(itinerary.getDescription()));
        dto.setActivities(convertStringToList(itinerary.getActivities()));
        dto.setMeals(itinerary.getMeals());
        dto.setAccommodation(itinerary.getAccommodation());
        return dto;
    }

    private TourScheduleDto mapScheduleToDto(TourSchedule schedule) {
        TourScheduleDto dto = new TourScheduleDto();
        dto.setDepartureDate(schedule.getDepartureDate());
        dto.setReturnDate(schedule.getReturnDate());
        dto.setSpecialPrice(schedule.getSpecialPrice());
        dto.setAvailableSlots(schedule.getAvailableSlots());
        dto.setStatus(schedule.getStatus());
        return dto;
    }

    private List<String> convertStringToList(String str) {
        if (str == null || str.trim().isEmpty()) {
            return List.of();
        }
        // Tách chuỗi HTML <p>...</p> thành list
        return List.of(str.split("<p>"))
                .stream()
                .map(s -> s.replace("</p>", "").trim())
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
