package ru.student.mediaportal.dto;

import jakarta.validation.constraints.*;
import ru.student.mediaportal.validation.HttpUrl;

public record EpisodeRequest(
        @NotNull @Positive Integer number,
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 2048) @HttpUrl String audioUrl,
        @NotNull @Positive Integer duration) {
}
