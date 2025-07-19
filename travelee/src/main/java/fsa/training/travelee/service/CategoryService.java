package fsa.training.travelee.service;

import fsa.training.travelee.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CategoryService {

    Page<Category> findAll(String keyword, Pageable pageable);

    Optional<Category> findById(Long id);

    Category save(Category category);

    void deleteById(Long id);
}
