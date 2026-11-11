package ru.student.mediaportal.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.student.mediaportal.dto.PodcastRequest;
import ru.student.mediaportal.dto.PodcastResponse;
import ru.student.mediaportal.service.PodcastService;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/podcasts")
@Tag(name = "Подкаст")
public class PodcastController {
    private final PodcastService service;

    public PodcastController(PodcastService service) {
        this.service = service;
    }

    @GetMapping
    public List<PodcastResponse> findAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public PodcastResponse findById(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    public ResponseEntity<PodcastResponse> create(@Valid @RequestBody PodcastRequest request) {
        PodcastResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/podcasts/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    public PodcastResponse update(@PathVariable Long id, @Valid @RequestBody PodcastRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
