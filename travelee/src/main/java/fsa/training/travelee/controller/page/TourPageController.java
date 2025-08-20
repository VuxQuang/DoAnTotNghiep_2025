package fsa.training.travelee.controller.page;

import fsa.training.travelee.dto.TourListClientDto;
import fsa.training.travelee.entity.Tour;
import fsa.training.travelee.service.TourClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class TourPageController {

    private final TourClientService tourClientService;

    @GetMapping({"/page/tours/newest", "/client/tours/newest"})
    public String getNewestTours(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            Model model) {

        // Xử lý trường hợp page < 0
        if (page < 0) {
            page = 0;
        }

        Page<TourListClientDto> newestTours = tourClientService.getNewestTours(page, size);

        // Đảm bảo đủ 6 slot
        List<TourListClientDto> tourList = new ArrayList<>(newestTours.getContent());
        while (tourList.size() < size) {
            tourList.add(null);
        }

        model.addAttribute("newestTours", tourList);
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("totalPages", newestTours.getTotalPages());
        return "page/tour/tour-list";
    }

    @GetMapping("/page/tours/{id}")
    public String getTourDetail(@PathVariable Long id, Model model) {
        try {
            Tour tour = tourClientService.getTourById(id);
            if (tour == null) {
                return "redirect:/page/tours/newest";
            }
            
            model.addAttribute("tour", tour);
            return "page/tour/tour-detail";
        } catch (Exception e) {
            return "redirect:/page/tours/newest";
        }
    }

    @GetMapping("/page/tours/{id}/book")
    public String bookTourWithSchedule(
            @PathVariable Long id,
            @RequestParam(required = false) Long scheduleId,
            Model model) {
        
        try {
            Tour tour = tourClientService.getTourById(id);
            if (tour == null) {
                return "redirect:/page/tours/newest";
            }
            
            // Kiểm tra xem scheduleId có hợp lệ không
            final Long finalScheduleId = scheduleId;
            if (finalScheduleId != null) {
                boolean validSchedule = tour.getSchedules().stream()
                    .anyMatch(schedule -> schedule.getId().equals(finalScheduleId));
                
                if (!validSchedule) {
                    scheduleId = null; // Reset nếu không hợp lệ
                }
            }
            
            // Chuyển hướng đến form booking với scheduleId
            String redirectUrl = "/page/booking/" + id;
            if (scheduleId != null) {
                redirectUrl += "?scheduleId=" + scheduleId;
            }
            
            return "redirect:" + redirectUrl;
            
        } catch (Exception e) {
            return "redirect:/page/tours/" + id;
        }
    }

}
