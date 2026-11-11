package ru.student.mediaportal.dto;

import java.time.LocalDate;

public record ArticleResponse(Long id, String title, String text, String author, LocalDate publicationDate) {
}
