package fsa.training.travelee.service;

import fsa.training.travelee.entity.Article;
import fsa.training.travelee.entity.CategoryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ArticleService {
    Page<Article> findAll(String keyword, CategoryType type, Pageable pageable);

    Optional<Article> findById(Long id);

    Article save(Article article);

    void deleteById(Long id);
}
