package fsa.training.travelee.controller.page;

import fsa.training.travelee.dto.TourListClientDto;
import fsa.training.travelee.service.TourClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class TourPageController {

    private final TourClientService tourClientService;

    @GetMapping("/page/tours/newest")
    public String getNewestTours(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            Model model) {

        Page<TourListClientDto> newestTours = tourClientService.getNewestTours(page, size);

        // Đảm bảo đủ 6 slot
        List<TourListClientDto> tourList = new ArrayList<>(newestTours.getContent());
        while (tourList.size() < size) {
            tourList.add(null);
        }

        model.addAttribute("newestTours", tourList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", newestTours.getTotalPages());
        return "page/tour/tour-list";
    }

}
