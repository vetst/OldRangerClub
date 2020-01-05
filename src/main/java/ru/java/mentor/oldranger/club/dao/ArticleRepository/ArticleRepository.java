package ru.java.mentor.oldranger.club.dao.ArticleRepository;

import org.hibernate.annotations.Parameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.java.mentor.oldranger.club.model.article.Article;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    List<Article> findAllByArticleTags_id(long id);

    Article findById(long id);


    void deleteAllByIdIn(List<Long> ids);
  
    @Query(nativeQuery = true,
            value = "select * from articles a where a.article_hide = false")
    Page<Article> getArticlesForAnon(Pageable pageable);
}