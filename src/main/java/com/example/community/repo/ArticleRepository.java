package com.example.community.repo;

import com.example.community.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    List<Article> findByTitleContainsAndBoardId(String query, Long boardId);
    List<Article> findByContentContainsAndBoardId(String query, Long boardId);
    List<Article> findByTitleContains(String query);
    List<Article> findByContentContains(String query);

    Optional<Article> findFirstByIdAfter(Long id);
    Optional<Article> findFirstByIdBeforeOrderByIdDesc(Long id);

    Optional<Article> findFirstByBoardIdAndIdAfter(Long boardId, Long id);
    Optional<Article> findFirstByBoardIdAndIdBeforeOrderByIdDesc(Long boardId, Long id);
}
