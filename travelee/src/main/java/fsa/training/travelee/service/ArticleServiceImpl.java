package fsa.training.travelee.service;

import fsa.training.travelee.entity.Article;
import fsa.training.travelee.entity.CategoryType;
import fsa.training.travelee.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService{

    private final ArticleRepository articleRepository;

    @Override
    public Page<Article> findAll(String keyword, CategoryType type, Pageable pageable) {
        return articleRepository.searchByCategoryType(keyword, type, pageable);
    }

    @Override
    public Optional<Article> findById(Long id) {
        return articleRepository.findById(id);
    }

    @Override
    public Article save(Article article) {
        return articleRepository.save(article);
    }

    @Override
    public void deleteById(Long id) {
        articleRepository.deleteById(id);
    }
}
