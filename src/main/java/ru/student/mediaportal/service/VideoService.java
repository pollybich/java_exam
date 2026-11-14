package ru.student.mediaportal.service;

import org.springframework.data.domain.Sort;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.student.mediaportal.dto.*;
import ru.student.mediaportal.entity.*;
import ru.student.mediaportal.repository.VideoRepository;
import ru.student.mediaportal.exception.NotFoundException;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class VideoService {
    private final VideoRepository repository;
    private final PopularityService popularity;

    public VideoService(VideoRepository repository, PopularityService popularity) {
        this.repository = repository;
        this.popularity = popularity;
    }

    public List<VideoResponse> findAll() {
        return repository.findAll(Sort.by("id")).stream().map(this::toResponse).toList();
    }

    @Cacheable(cacheNames = "videos", key = "#id")
    public VideoResponse findById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional
    public VideoResponse create(VideoRequest request) {
        Video entity = new Video();
        fill(entity, request);
        return toResponse(repository.save(entity));
    }

    @Transactional
    @CacheEvict(cacheNames = "videos", key = "#id")
    public VideoResponse update(Long id, VideoRequest request) {
        Video entity = getEntity(id);
        fill(entity, request);
        return toResponse(repository.save(entity));
    }

    @Transactional
    @CacheEvict(cacheNames = "videos", key = "#id")
    public void delete(Long id) {
        repository.delete(getEntity(id));
        popularity.remove("videos", id);
    }

    private Video getEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Видео", id));
    }

    private void fill(Video entity, VideoRequest request) {
        entity.setTitle(request.title());
        entity.setUrl(request.url());
        entity.setDuration(request.duration());
    }

    private VideoResponse toResponse(Video entity) {
        return new VideoResponse(entity.getId(), entity.getTitle(), entity.getUrl(), entity.getDuration());
    }
}
