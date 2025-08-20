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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

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
            @RequestParam(defaultValue = "0") Long categoryId,
            Model model
    ) {
        // Gọi phương thức tìm kiếm bài viết theo keyword và categoryId
        Page<Article> articlePage = articleService.findAll(keyword, categoryId, CategoryType.ARTICLE, PageRequest.of(page, size));

        // Thêm dữ liệu vào model
        model.addAttribute("articles", articlePage.getContent());
        model.addAttribute("totalItems", articlePage.getTotalElements());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", articlePage.getTotalPages());
        model.addAttribute("size", size);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("categories", categoryService.findByType(CategoryType.ARTICLE)); // Danh mục bài viết

        return "/page/article/article"; // View
    }

    @GetMapping("/{id}")
    public String showArticleDetail(@PathVariable("id") Long id, Model model) {
        Article article = articleService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài viết"));

        List<Article> latestArticles = articleService.findTop3LatestArticles();

        model.addAttribute("categories", categoryService.findByType(CategoryType.ARTICLE));
        model.addAttribute("article", article);
        model.addAttribute("latestArticles", latestArticles);

        return "page/article/article-detail";
    }


}
