package ru.student.mediaportal.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.student.mediaportal.dto.*;
import ru.student.mediaportal.entity.*;
import ru.student.mediaportal.exception.NotFoundException;
import ru.student.mediaportal.repository.PodcastRepository;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PodcastServiceTest {
    @Mock private PodcastRepository repository;
    @InjectMocks private PodcastService service;
    private final PodcastRequest request = new PodcastRequest("Подкаст", List.of(new EpisodeRequest(1, "Эпизод", "https://example.com/1.mp3", 600)));

    @Test
    void createsContent() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var result = service.create(request);
        assertEquals("Подкаст", result.title());
        assertEquals(1, result.episodes().size());
        assertEquals(1, result.episodes().getFirst().number());
        ArgumentCaptor<Podcast> captor = ArgumentCaptor.forClass(Podcast.class);
        verify(repository).save(captor.capture());
        assertSame(captor.getValue(), captor.getValue().getEpisodes().getFirst().getPodcast());
    }

    @Test
    void readsExistingContent() {
        Podcast entity = new Podcast();
        entity.setTitle("Подкаст");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        assertEquals("Подкаст", service.findById(1L).title());
    }

    @Test
    void updatesExistingContent() {
        Podcast entity = new Podcast();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var result = service.update(1L, new PodcastRequest("Новый заголовок", List.of(new EpisodeRequest(2, "Второй", "https://example.com/2.mp3", 300))));
        assertEquals("Новый заголовок", result.title());
        verify(repository).flush();
        assertEquals(1, result.episodes().size());
        assertEquals(2, result.episodes().getFirst().number());
    }

    @Test
    void deletesExistingContent() {
        Podcast entity = new Podcast();
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
