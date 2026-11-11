package ru.student.mediaportal.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record ArticleRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank String text,
        @NotBlank @Size(max = 255) String author,
        @NotNull LocalDate publicationDate) {
}
