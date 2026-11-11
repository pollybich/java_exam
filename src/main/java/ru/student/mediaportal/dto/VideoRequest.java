package ru.student.mediaportal.dto;

import jakarta.validation.constraints.*;
import ru.student.mediaportal.validation.HttpUrl;

public record VideoRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 2048) @HttpUrl String url,
        @NotNull @Positive Integer duration) {
}
