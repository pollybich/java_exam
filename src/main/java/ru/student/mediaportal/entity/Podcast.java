package ru.student.mediaportal.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "podcasts")
public class Podcast {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @OneToMany(mappedBy = "podcast", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("number ASC")
    private List<Episode> episodes = new ArrayList<>();

    public List<Episode> getEpisodes() { return episodes; }

    public void addEpisode(Episode episode) {
        episodes.add(episode);
        episode.setPodcast(this);
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
