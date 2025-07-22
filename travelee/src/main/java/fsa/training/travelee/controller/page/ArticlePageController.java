package fsa.training.travelee.controller.page;

import fsa.training.travelee.entity.Article;
import fsa.training.travelee.entity.CategoryType;
import fsa.training.travelee.service.ArticleService;
import fsa.training.travelee.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/page/article")
public class ArticlePageController {
    private final ArticleService articleService;

    private final CategoryService categoryService;

    @GetMapping
    public String getNewsPage(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model
    ) {

        Page<Article> articlePage = articleService.findAll(keyword, CategoryType.ARTICLE, PageRequest.of(page, size));
        model.addAttribute("articles", articlePage.getContent());
        model.addAttribute("totalItems", articlePage.getTotalElements());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", articlePage.getTotalPages());
        model.addAttribute("size", size);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categories", categoryService.findByType(CategoryType.ARTICLE));

        return "/page/article/article";
    }
}
