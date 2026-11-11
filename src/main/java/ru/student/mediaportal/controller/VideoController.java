package ru.student.mediaportal.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.student.mediaportal.dto.VideoRequest;
import ru.student.mediaportal.dto.VideoResponse;
import ru.student.mediaportal.service.VideoService;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/videos")
@Tag(name = "Видео")
public class VideoController {
    private final VideoService service;

    public VideoController(VideoService service) {
        this.service = service;
    }

    @GetMapping
    public List<VideoResponse> findAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public VideoResponse findById(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    public ResponseEntity<VideoResponse> create(@Valid @RequestBody VideoRequest request) {
        VideoResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/videos/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    public VideoResponse update(@PathVariable Long id, @Valid @RequestBody VideoRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
