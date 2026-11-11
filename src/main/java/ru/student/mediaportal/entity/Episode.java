package ru.student.mediaportal.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "episodes")
public class Episode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer number;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2048)
    private String audioUrl;

    @Column(nullable = false)
    private Integer duration;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "podcast_id", nullable = false)
    private Podcast podcast;

    public Long getId() { return id; }
    public Integer getNumber() { return number; }
    public void setNumber(Integer number) { this.number = number; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public Podcast getPodcast() { return podcast; }
    public void setPodcast(Podcast podcast) { this.podcast = podcast; }
}
