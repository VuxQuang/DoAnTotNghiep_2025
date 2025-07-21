package fsa.training.travelee.controller.page;

import fsa.training.travelee.service.ArticleService;
import fsa.training.travelee.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleService articleService;

    private final CategoryService categoryService;
}
