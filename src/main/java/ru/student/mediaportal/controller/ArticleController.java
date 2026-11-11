package ru.student.mediaportal.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.student.mediaportal.dto.ArticleRequest;
import ru.student.mediaportal.dto.ArticleResponse;
import ru.student.mediaportal.service.ArticleService;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/articles")
@Tag(name = "Статья")
public class ArticleController {
    private final ArticleService service;

    public ArticleController(ArticleService service) {
        this.service = service;
    }

    @GetMapping
    public List<ArticleResponse> findAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public ArticleResponse findById(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    public ResponseEntity<ArticleResponse> create(@Valid @RequestBody ArticleRequest request) {
        ArticleResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/articles/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    public ArticleResponse update(@PathVariable Long id, @Valid @RequestBody ArticleRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
