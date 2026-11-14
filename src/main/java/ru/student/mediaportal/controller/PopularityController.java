package ru.student.mediaportal.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.student.mediaportal.dto.PopularContentResponse;
import ru.student.mediaportal.service.PopularityService;
import java.util.List;

@RestController
@RequestMapping("/api/popular")
@Tag(name = "Популярный контент")
public class PopularityController {
    private final PopularityService service;

    public PopularityController(PopularityService service) {
        this.service = service;
    }

    @PostMapping("/{type}/{id}/views")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> recordView(@PathVariable String type, @PathVariable Long id) {
        service.recordView(type, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<PopularContentResponse> top() {
        return service.top();
    }
}
