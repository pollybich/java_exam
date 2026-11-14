package ru.student.mediaportal.service;

import org.springframework.data.domain.Sort;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.student.mediaportal.dto.*;
import ru.student.mediaportal.entity.*;
import ru.student.mediaportal.repository.ArticleRepository;
import ru.student.mediaportal.exception.NotFoundException;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ArticleService {
    private final ArticleRepository repository;
    private final PopularityService popularity;

    public ArticleService(ArticleRepository repository, PopularityService popularity) {
        this.repository = repository;
        this.popularity = popularity;
    }

    public List<ArticleResponse> findAll() {
        return repository.findAll(Sort.by("id")).stream().map(this::toResponse).toList();
    }

    @Cacheable(cacheNames = "articles", key = "#id")
    public ArticleResponse findById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional
    public ArticleResponse create(ArticleRequest request) {
        Article entity = new Article();
        fill(entity, request);
        return toResponse(repository.save(entity));
    }

    @Transactional
    @CacheEvict(cacheNames = "articles", key = "#id")
    public ArticleResponse update(Long id, ArticleRequest request) {
        Article entity = getEntity(id);
        fill(entity, request);
        return toResponse(repository.save(entity));
    }

    @Transactional
    @CacheEvict(cacheNames = "articles", key = "#id")
    public void delete(Long id) {
        repository.delete(getEntity(id));
        popularity.remove("articles", id);
    }

    private Article getEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Статья", id));
    }

    private void fill(Article entity, ArticleRequest request) {
        entity.setTitle(request.title());
        entity.setText(request.text());
        entity.setAuthor(request.author());
        entity.setPublicationDate(request.publicationDate());
    }

    private ArticleResponse toResponse(Article entity) {
        return new ArticleResponse(entity.getId(), entity.getTitle(), entity.getText(), entity.getAuthor(), entity.getPublicationDate());
    }
}
