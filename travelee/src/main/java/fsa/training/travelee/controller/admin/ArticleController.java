package fsa.training.travelee.controller.admin;

import fsa.training.travelee.entity.Article;
import fsa.training.travelee.entity.ArticleStatus;
import fsa.training.travelee.entity.User;
import fsa.training.travelee.service.ArticleService;
import fsa.training.travelee.service.CategoryService;
import fsa.training.travelee.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import fsa.training.travelee.entity.CategoryType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/article")
public class ArticleController {

    private final ArticleService articleService;

    private final CategoryService categoryService;
    
    private final UserService userService;

    @GetMapping("/list")
    public String listArticles(@RequestParam(defaultValue = "") String keyword,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "5") int size,
                               @RequestParam(defaultValue = "0") Long categoryId,
                               @RequestParam(defaultValue = "ARTICLE") CategoryType categoryType,
                               Model model) {

        Page<Article> articlePage = articleService.findAll(keyword, categoryId, categoryType, PageRequest.of(page, size));
        model.addAttribute("articles", articlePage.getContent());
        model.addAttribute("totalItems", articlePage.getTotalElements());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", articlePage.getTotalPages());
        model.addAttribute("size", size);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categories", categoryService.findByType(CategoryType.ARTICLE));

        return "admin/article/article";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("article", new Article());
        model.addAttribute("categories", categoryService.findByType(CategoryType.ARTICLE));
        model.addAttribute("isUpdate", false);
        model.addAttribute("isReadOnly", false);
        return "admin/article/create-article";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Article article = articleService.findById(id).orElseThrow(() -> new RuntimeException("Article not found"));
        model.addAttribute("article", article);
        model.addAttribute("categories", categoryService.findByType(CategoryType.ARTICLE));
        model.addAttribute("isUpdate", true);
        model.addAttribute("isReadOnly", false);
        return "admin/article/create-article";
    }

    @GetMapping("/view/{id}")
    public String viewArticle(@PathVariable Long id, Model model) {
        Article article = articleService.findById(id).orElseThrow(() -> new RuntimeException("Article not found"));
        model.addAttribute("article", article);
        model.addAttribute("categories", categoryService.findByType(CategoryType.ARTICLE));
        model.addAttribute("isUpdate", false);
        model.addAttribute("isReadOnly", true);
        return "admin/article/create-article";
    }

    @PostMapping("/save")
    public String saveArticle(@ModelAttribute("article") Article article,
                              @RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
                              BindingResult bindingResult,
                              Model model) {
        try {
            if (article.getTitle() == null || article.getTitle().trim().isEmpty()) {
                bindingResult.rejectValue("title", "error.title", "Tiêu đề không được để trống");
            }
            
            if (article.getDescription() == null || article.getDescription().trim().isEmpty()) {
                bindingResult.rejectValue("description", "error.description", "Mô tả không được để trống");
            }
            
            if (article.getContent() == null || article.getContent().trim().isEmpty()) {
                bindingResult.rejectValue("content", "error.content", "Nội dung không được để trống");
            }
            
            if (article.getCategories() == null || article.getCategories().isEmpty()) {
                bindingResult.rejectValue("categories", "error.categories", "Vui lòng chọn ít nhất một danh mục");
            }
            
            // Nếu có lỗi validation, trả về form với lỗi
            if (bindingResult.hasErrors()) {
                model.addAttribute("categories", categoryService.findByType(CategoryType.ARTICLE));
                model.addAttribute("isUpdate", article.getId() != null);
                model.addAttribute("isReadOnly", false);
                return "admin/article/create-article";
            }
            
            // Set user (author) cho article
            User currentUser = userService.getCurrentUser();
            if (currentUser != null) {
                article.setUser(currentUser);
            } else {
                // Nếu không có user đăng nhập, có thể set một user mặc định hoặc throw exception
                throw new RuntimeException("Không tìm thấy người dùng đăng nhập");
            }
            
            // Set status mặc định cho article mới
            if (article.getId() == null) {
                article.setStatus(ArticleStatus.ACTIVE);
            }

            if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
                String uploadDir = "uploads/";
                Path uploadPath = Paths.get(uploadDir);
                
                // Tạo thư mục nếu chưa tồn tại
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                String filename = UUID.randomUUID() + "_" + thumbnailFile.getOriginalFilename();
                Path filePath = uploadPath.resolve(filename);
                Files.copy(thumbnailFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                article.setThumbnail("/uploads/" + filename);
            }

            Article savedArticle = articleService.save(article);
            System.out.println("Article saved successfully with ID: " + savedArticle.getId());
            
        } catch (IOException e) {
            System.err.println("Error uploading file: " + e.getMessage());
            e.printStackTrace();
            bindingResult.rejectValue("thumbnailFile", "error.thumbnailFile", "Lỗi khi upload file: " + e.getMessage());
            model.addAttribute("categories", categoryService.findByType(CategoryType.ARTICLE));
            model.addAttribute("isUpdate", article.getId() != null);
            model.addAttribute("isReadOnly", false);
            return "admin/article/create-article";
        } catch (Exception e) {
            System.err.println("Error saving article: " + e.getMessage());
            e.printStackTrace();
            bindingResult.rejectValue("title", "error.general", "Lỗi khi lưu bài viết: " + e.getMessage());
            model.addAttribute("categories", categoryService.findByType(CategoryType.ARTICLE));
            model.addAttribute("isUpdate", article.getId() != null);
            model.addAttribute("isReadOnly", false);
            return "admin/article/create-article";
        }

        return "redirect:/admin/article/list";
    }
    @GetMapping("/delete/{id}")
    public String deleteArticle(@PathVariable Long id) {
        articleService.deleteById(id);
        return "redirect:/admin/article/list";
    }
}
