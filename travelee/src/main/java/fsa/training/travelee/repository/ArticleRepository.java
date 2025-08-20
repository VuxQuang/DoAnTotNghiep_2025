package fsa.training.travelee.repository;

import fsa.training.travelee.entity.Article;
import fsa.training.travelee.entity.CategoryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    @Query("SELECT DISTINCT a FROM Article a " +
            "JOIN a.categories c " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:categoryId = 0 OR c.id = :categoryId) " +
            "AND (:categoryType IS NULL OR c.type = :categoryType)")
    Page<Article> searchByCategoryAndType(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("categoryType") CategoryType categoryType,
            Pageable pageable
    );

    @Query("SELECT a FROM Article a JOIN FETCH a.user u ORDER BY a.createdAt DESC")
    List<Article> findTop3LatestArticles();


}
