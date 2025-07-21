package fsa.training.travelee.repository;

import fsa.training.travelee.entity.Category;
import fsa.training.travelee.entity.CategoryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface CategoryRepository extends JpaRepository<Category, Long> {
    Page<Category> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    List<Category> findByType(CategoryType type);
}
