package ru.student.mediaportal.dto;

import java.util.List;

public record PodcastResponse(Long id, String title, List<EpisodeResponse> episodes) {
}
