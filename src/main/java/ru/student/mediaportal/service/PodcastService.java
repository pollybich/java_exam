package ru.student.mediaportal.service;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.student.mediaportal.dto.*;
import ru.student.mediaportal.entity.*;
import ru.student.mediaportal.repository.PodcastRepository;
import ru.student.mediaportal.exception.NotFoundException;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PodcastService {
    private final PodcastRepository repository;

    public PodcastService(PodcastRepository repository) {
        this.repository = repository;
    }

    public List<PodcastResponse> findAll() {
        return repository.findAll(Sort.by("id")).stream().map(this::toResponse).toList();
    }

    public PodcastResponse findById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional
    public PodcastResponse create(PodcastRequest request) {
        Podcast entity = new Podcast();
        fill(entity, request);
        return toResponse(repository.save(entity));
    }

    @Transactional
    public PodcastResponse update(Long id, PodcastRequest request) {
        Podcast entity = getEntity(id);
        // Удаляем прежние эпизоды до вставки новых с теми же номерами.
        entity.getEpisodes().clear();
        repository.flush();
        fill(entity, request);
        return toResponse(repository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(getEntity(id));
    }

    private Podcast getEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Подкаст", id));
    }

    private void fill(Podcast entity, PodcastRequest request) {
        entity.setTitle(request.title());
        for (EpisodeRequest item : request.episodes()) {
            Episode episode = new Episode();
            episode.setNumber(item.number());
            episode.setTitle(item.title());
            episode.setAudioUrl(item.audioUrl());
            episode.setDuration(item.duration());
            entity.addEpisode(episode);
        }
    }

    private PodcastResponse toResponse(Podcast entity) {
        return new PodcastResponse(entity.getId(), entity.getTitle(), entity.getEpisodes().stream()
                .sorted(java.util.Comparator.comparing(Episode::getNumber))
                .map(e -> new EpisodeResponse(e.getNumber(), e.getTitle(), e.getAudioUrl(), e.getDuration()))
                .toList());
    }
}
