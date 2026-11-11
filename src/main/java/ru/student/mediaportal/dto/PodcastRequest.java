package ru.student.mediaportal.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.HashSet;

public record PodcastRequest(
        @NotBlank @Size(max = 255) String title,
        @NotNull List<@NotNull @Valid EpisodeRequest> episodes) {

    @JsonIgnore
    @AssertTrue(message = "Номера эпизодов не должны повторяться")
    public boolean isEpisodeNumbersUnique() {
        if (episodes == null) return true;
        var numbers = new HashSet<Integer>();
        for (EpisodeRequest episode : episodes) {
            if (episode != null && !numbers.add(episode.number())) return false;
        }
        return true;
    }
}
