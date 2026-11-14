package ru.student.mediaportal.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.student.mediaportal.dto.PopularContentResponse;
import ru.student.mediaportal.exception.NotFoundException;
import ru.student.mediaportal.repository.ArticleRepository;
import ru.student.mediaportal.repository.PodcastRepository;
import ru.student.mediaportal.repository.VideoRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class PopularityService {
    private final StringRedisTemplate redis;
    private final ArticleRepository articles;
    private final VideoRepository videos;
    private final PodcastRepository podcasts;
    private final Clock clock;

    public PopularityService(StringRedisTemplate redis, ArticleRepository articles,
            VideoRepository videos, PodcastRepository podcasts, Clock clock) {
        this.redis = redis;
        this.articles = articles;
        this.videos = videos;
        this.podcasts = podcasts;
        this.clock = clock;
    }

    public void recordView(String type, Long id) {
        boolean exists = switch (type) {
            case "articles" -> articles.existsById(id);
            case "videos" -> videos.existsById(id);
            case "podcasts" -> podcasts.existsById(id);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неизвестный тип контента");
        };
        if (!exists) throw new NotFoundException("Контент", id);
        String key = currentHourKey();
        redis.opsForZSet().incrementScore(key, type + ":" + id, 1);
        redis.expire(key, Duration.ofHours(2));
    }

    public List<PopularContentResponse> top() {
        var entries = redis.opsForZSet().reverseRangeWithScores(currentHourKey(), 0, 9);
        if (entries == null) return List.of();
        return entries.stream().map(entry -> {
            String[] parts = entry.getValue().split(":");
            return new PopularContentResponse(parts[0], Long.valueOf(parts[1]), entry.getScore().longValue());
        }).toList();
    }

    public void remove(String type, Long id) {
        redis.opsForZSet().remove(currentHourKey(), type + ":" + id);
    }

    private String currentHourKey() {
        return "popular:" + clock.instant().truncatedTo(ChronoUnit.HOURS).getEpochSecond();
    }
}
