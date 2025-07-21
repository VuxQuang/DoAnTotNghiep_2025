package fsa.training.travelee.service;

import fsa.training.travelee.entity.Category;
import fsa.training.travelee.entity.CategoryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CategoryService {

    Page<Category> findAll(String keyword, Pageable pageable);

    Optional<Category> findById(Long id);

    Category save(Category category);

    void deleteById(Long id);

    List<Category> findAll();

    List<Category> findByType(CategoryType type);
}
