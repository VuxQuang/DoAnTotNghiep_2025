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
import fsa.training.travelee.entity.Tour;
import fsa.training.travelee.mapper.TourMapper;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/tour")
public class TourAdminController {

    private final TourService tourService;
    private final CategoryRepository categoryRepository;
    private final TourMapper tourMapper;

    @GetMapping("/list")
    public String showTourList(Model model) {
        List<TourListDto> tours = tourService.getAllTours();
        model.addAttribute("tours", tours);
        return "admin/tour/tour-page";
    }

    @GetMapping("/view/{id}")
    public String viewTour(@PathVariable Long id, Model model) {
        try {
            Tour tour = tourService.getById(id);
            TourCreateRequest tourRequest = tourMapper.toDto(tour);
            List<Category> categories = categoryRepository.findByType(CategoryType.TOUR);
            
            model.addAttribute("tour", tour);
            model.addAttribute("tourCreateRequest", tourRequest);
            model.addAttribute("categories", categories);
            model.addAttribute("isReadOnly", true);
            
            return "admin/tour/create-tour";
        } catch (RuntimeException e) {
            System.err.println(">>> Error loading tour: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/admin/tour/list";
        }
    }

    @GetMapping("/{id}")
    public String showTourDetail(@PathVariable Long id, Model model) {
        // Redirect to view method for readonly display
        return "redirect:/admin/tour/view/" + id;
    }

    @GetMapping("/create")
    public String showCreateTourForm(Model model) {
        System.out.println("=== CREATE TOUR FORM DEBUG ===");
        List<Category> categories = categoryRepository.findByType(CategoryType.TOUR);
        model.addAttribute("categories", categories);
        model.addAttribute("tourCreateRequest", new TourCreateRequest());
        model.addAttribute("isReadOnly", false);
        System.out.println("isReadOnly: false");
        System.out.println("==============================");
        return "admin/tour/create-tour";
    }

    @PostMapping("/create")
    public String createTour(@ModelAttribute TourCreateRequest request) {
        tourService.createTour(request);
//        System.out.println(">>> DESCRIPTION: " + request.getDescription());
        return "redirect:/admin/tour/list";
    }

    @GetMapping("/edit/{id}")
    public String showEditTourForm(@PathVariable Long id, Model model) {
        try {
            Tour tour = tourService.getById(id);
            TourCreateRequest tourRequest = tourMapper.toDto(tour);
            List<Category> categories = categoryRepository.findByType(CategoryType.TOUR);
            
            model.addAttribute("tour", tour);
            model.addAttribute("tourCreateRequest", tourRequest);
            model.addAttribute("categories", categories);
            model.addAttribute("isReadOnly", false);
            model.addAttribute("isUpdate", true);
            
            return "admin/tour/edit-tour";
        } catch (RuntimeException e) {
            return "redirect:/admin/tour/list";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateTour(@PathVariable Long id, @ModelAttribute TourCreateRequest request) {
        try {
            tourService.updateTour(id, request);
            return "redirect:/admin/tour/list";
        } catch (RuntimeException e) {
            return "redirect:/admin/tour/edit/" + id;
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteTour(@PathVariable Long id) {
        try {
            tourService.deleteTourById(id);
        } catch (RuntimeException e) {
            // Xử lý lỗi nếu cần
        }
        return "redirect:/admin/tour/list";
    }
}
