package ru.student.mediaportal.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.student.mediaportal.dto.*;
import ru.student.mediaportal.entity.*;
import ru.student.mediaportal.exception.NotFoundException;
import ru.student.mediaportal.repository.ArticleRepository;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {
    @Mock private ArticleRepository repository;
    @InjectMocks private ArticleService service;
    private final ArticleRequest request = new ArticleRequest("Статья", "Текст", "Иван", LocalDate.of(2026, 1, 1));

    @Test
    void createsContent() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var result = service.create(request);
        assertEquals("Статья", result.title());
    }

    @Test
    void readsExistingContent() {
        Article entity = new Article();
        entity.setTitle("Статья");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        assertEquals("Статья", service.findById(1L).title());
    }

    @Test
    void updatesExistingContent() {
        Article entity = new Article();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var result = service.update(1L, new ArticleRequest("Новый заголовок", "Новый текст", "Анна", LocalDate.of(2026, 2, 1)));
        assertEquals("Новый заголовок", result.title());
    }

    @Test
    void deletesExistingContent() {
        Article entity = new Article();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        service.delete(1L);
        verify(repository).delete(entity);
    }

    @Test
    void rejectsOperationsWithMissingContent() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.findById(99L));
        assertThrows(NotFoundException.class, () -> service.update(99L, request));
        assertThrows(NotFoundException.class, () -> service.delete(99L));
        verify(repository, never()).save(any());
        verify(repository, never()).delete(any());
    }
}
