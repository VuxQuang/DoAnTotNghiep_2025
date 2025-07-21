package fsa.training.travelee.repository;

import fsa.training.travelee.entity.Article;
import fsa.training.travelee.entity.CategoryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    @Query("SELECT DISTINCT a FROM Article a JOIN a.categories c " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:type IS NULL OR c.type = :type)")
    Page<Article> searchByCategoryType(
            @Param("keyword") String keyword,
            @Param("type") CategoryType type,
            Pageable pageable
    );
}
