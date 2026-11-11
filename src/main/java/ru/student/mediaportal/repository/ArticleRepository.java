package ru.student.mediaportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.student.mediaportal.entity.Article;

public interface ArticleRepository extends JpaRepository<Article, Long> {
}
