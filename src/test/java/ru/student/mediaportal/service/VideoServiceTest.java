package ru.student.mediaportal.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.student.mediaportal.dto.*;
import ru.student.mediaportal.entity.*;
import ru.student.mediaportal.exception.NotFoundException;
import ru.student.mediaportal.repository.VideoRepository;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoServiceTest {
    @Mock private VideoRepository repository;
    @InjectMocks private VideoService service;
    private final VideoRequest request = new VideoRequest("Видео", "https://example.com/video.mp4", 60);

    @Test
    void createsContent() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var result = service.create(request);
        assertEquals("Видео", result.title());
    }

    @Test
    void readsExistingContent() {
        Video entity = new Video();
        entity.setTitle("Видео");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        assertEquals("Видео", service.findById(1L).title());
    }

    @Test
    void updatesExistingContent() {
        Video entity = new Video();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var result = service.update(1L, new VideoRequest("Новый заголовок", "https://example.com/new.mp4", 120));
        assertEquals("Новый заголовок", result.title());
    }

    @Test
    void deletesExistingContent() {
        Video entity = new Video();
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
