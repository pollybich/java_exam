package ru.student.mediaportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.student.mediaportal.entity.Podcast;

public interface PodcastRepository extends JpaRepository<Podcast, Long> {
}
