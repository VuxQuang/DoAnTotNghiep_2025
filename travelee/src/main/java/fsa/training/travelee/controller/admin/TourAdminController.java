package fsa.training.travelee.controller.admin;

import fsa.training.travelee.dto.TourCreateRequest;
import fsa.training.travelee.dto.TourListDto;
import fsa.training.travelee.entity.Category;
import fsa.training.travelee.entity.CategoryType;
import fsa.training.travelee.service.TourService;
import fsa.training.travelee.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/tour")
public class TourAdminController {

    private final TourService tourService;
    private final CategoryRepository categoryRepository;

    @GetMapping("/list")
    public String showTourList(Model model) {
        List<TourListDto> tours = tourService.getAllTours();
        model.addAttribute("tours", tours);
        return "admin/tour/tour-page";
    }


    @GetMapping("/create")
    public String showCreateTourForm(Model model) {
        List<Category> categories = categoryRepository.findByType(CategoryType.TOUR);
        model.addAttribute("categories", categories);
        model.addAttribute("tourCreateRequest", new TourCreateRequest());
        return "admin/tour/create-tour";
    }

    @PostMapping("/create")
    public String createTour(@ModelAttribute TourCreateRequest request) {
        tourService.createTour(request);
//        System.out.println(">>> DESCRIPTION: " + request.getDescription());
        return "redirect:/admin/tour/list";
    }
}
