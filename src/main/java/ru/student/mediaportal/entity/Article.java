package ru.student.mediaportal.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "articles")
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String text;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private LocalDate publicationDate;

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public LocalDate getPublicationDate() { return publicationDate; }
    public void setPublicationDate(LocalDate publicationDate) { this.publicationDate = publicationDate; }
}
