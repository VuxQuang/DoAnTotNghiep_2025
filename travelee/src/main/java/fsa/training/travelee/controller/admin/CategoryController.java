package fsa.training.travelee.controller.admin;

import fsa.training.travelee.entity.Category;
import fsa.training.travelee.entity.CategoryStatus;
import fsa.training.travelee.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/category")
public class CategoryController {

    private final CategoryService categoryService;


    @GetMapping("/list")
    public String listCategories(Model model,
                                 @RequestParam(defaultValue = "") String keyword,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "5") int size) {

        Page<Category> categoryPage = categoryService.findAll(keyword, PageRequest.of(page, size));

        model.addAttribute("categories", categoryPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", categoryPage.getTotalPages());
        model.addAttribute("totalCategories", categoryPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("keyword", keyword);

        return "admin/category/category";
    }

    @GetMapping("/create")
    public String createCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("isUpdate", false);
        return "admin/category/create-category";
    }

    @GetMapping("/edit/{id}")
    public String editCategoryForm(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        Category category = categoryService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + id));

        model.addAttribute("category", category);
        model.addAttribute("isUpdate", true);
        return "admin/category/create-category";
    }

    @PostMapping("/save")
    public String saveCategory(@ModelAttribute("category") Category category,
                               RedirectAttributes ra) {
        if (category.getId() == null) {
            category.setStatus(CategoryStatus.ACTIVE);
        }

        categoryService.save(category);
        ra.addFlashAttribute("success", "Lưu danh mục thành công!");
        return "redirect:/admin/category/list";
    }

    // 5. Xoá danh mục
    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id, RedirectAttributes ra) {
        categoryService.deleteById(id);
        ra.addFlashAttribute("success", "Xoá danh mục thành công!");
        return "redirect:/admin/category/list";
    }
}
