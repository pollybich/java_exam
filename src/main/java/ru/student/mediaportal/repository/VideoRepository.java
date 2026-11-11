package ru.student.mediaportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.student.mediaportal.entity.Video;

public interface VideoRepository extends JpaRepository<Video, Long> {
}
