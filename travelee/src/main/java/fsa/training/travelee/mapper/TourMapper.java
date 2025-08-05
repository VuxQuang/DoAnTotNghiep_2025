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
                .adultPrice(request.getAdultPrice())
                .childPrice(request.getChildPrice())
                .maxParticipants(request.getMaxParticipants())
                .status(request.getStatus())
                .featured(request.getFeatured())
                .isHot(request.getIsHot())
                .hasPromotion(request.getHasPromotion())
                .includes(request.getIncludes())
                .excludes(request.getExcludes())
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
                        .description(itineraryDto.getDescription())
                        .activities(itineraryDto.getActivities())
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
}
