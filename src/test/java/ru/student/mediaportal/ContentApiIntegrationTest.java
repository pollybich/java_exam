package ru.student.mediaportal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.stream.Stream;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ContentApiIntegrationTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void cleanDatabase() {
        jdbc.execute("TRUNCATE articles, videos, podcasts, episodes RESTART IDENTITY CASCADE");
    }

    static Stream<Arguments> contentCases() {
        return Stream.of(
                Arguments.of("articles", "{\"title\":\"Статья\",\"text\":\"Текст\",\"author\":\"Иван\",\"publicationDate\":\"2026-01-01\"}"),
                Arguments.of("videos", "{\"title\":\"Видео\",\"url\":\"https://example.com/video.mp4\",\"duration\":60}"),
                Arguments.of("podcasts", "{\"title\":\"Подкаст\",\"episodes\":[{\"number\":1,\"title\":\"Первый\",\"audioUrl\":\"https://example.com/1.mp3\",\"duration\":600}]}"));
    }

    @ParameterizedTest
    @MethodSource("contentCases")
    void fullCrud(String type, String body) throws Exception {
        String url = "/api/" + type;
        mvc.perform(get(url)).andExpect(status().isOk()).andExpect(content().json("[]"));
        String created = mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andExpect(content().json(body))
                .andExpect(header().exists("Location"))
                .andReturn().getResponse().getContentAsString();
        JsonNode entity = mapper.readTree(created);
        String itemUrl = url + "/" + entity.get("id").asLong();
        mvc.perform(get(itemUrl)).andExpect(status().isOk()).andExpect(content().json(created));
        mvc.perform(get(url)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        var updated = mapper.readTree(body).deepCopy();
        ((com.fasterxml.jackson.databind.node.ObjectNode) updated).put("title", "Изменено");
        mvc.perform(put(itemUrl).contentType(MediaType.APPLICATION_JSON).content(updated.toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Изменено"));
        mvc.perform(get(itemUrl)).andExpect(jsonPath("$.title").value("Изменено"));
        mvc.perform(delete(itemUrl)).andExpect(status().isNoContent()).andExpect(content().string(""));
        mvc.perform(get(itemUrl)).andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));
        mvc.perform(put(itemUrl).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isNotFound());
        mvc.perform(delete(itemUrl)).andExpect(status().isNotFound());
        if (type.equals("podcasts")) assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM episodes", Integer.class));
    }

    @ParameterizedTest
    @MethodSource("contentCases")
    void invalidCreateAndUpdateDoNotChangeData(String type, String body) throws Exception {
        String url = "/api/" + type;
        String created = mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String itemUrl = url + "/" + mapper.readTree(created).get("id").asLong();
        var invalid = (com.fasterxml.jackson.databind.node.ObjectNode) mapper.readTree(body);
        invalid.put("title", "   ");
        mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(invalid.toString()))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.title").exists());
        mvc.perform(put(itemUrl).contentType(MediaType.APPLICATION_JSON).content(invalid.toString()))
                .andExpect(status().isBadRequest());
        mvc.perform(get(itemUrl)).andExpect(content().json(created));
        mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
        mvc.perform(get(url)).andExpect(jsonPath("$.length()").value(1));
    }

    static Stream<Arguments> invalidFields() {
        return Stream.of(
                Arguments.of("articles", "{\"title\":\"A\",\"text\":\"\",\"author\":\"\",\"publicationDate\":\"2026-01-01\"}"),
                Arguments.of("articles", "{\"title\":\"A\",\"text\":\"Text\",\"author\":\"I\",\"publicationDate\":\"not-a-date\"}"),
                Arguments.of("videos", "{\"title\":\"V\",\"url\":\"bad-url\",\"duration\":0}"),
                Arguments.of("videos", "{\"title\":\"V\",\"url\":\"ftp://example.com/v\",\"duration\":10}"),
                Arguments.of("podcasts", "{\"title\":\"P\",\"episodes\":[{\"number\":0,\"title\":\"\",\"audioUrl\":\"bad\",\"duration\":-1}]}"),
                Arguments.of("podcasts", "{\"title\":\"P\",\"episodes\":[null]}"),
                Arguments.of("podcasts", "{\"title\":\"P\",\"episodes\":[{\"number\":1,\"title\":\"A\",\"audioUrl\":\"https://example.com/a\",\"duration\":10},{\"number\":1,\"title\":\"B\",\"audioUrl\":\"https://example.com/b\",\"duration\":20}]}"));
    }

    @ParameterizedTest
    @MethodSource("invalidFields")
    void rejectsInvalidFields(String type, String body) throws Exception {
        mvc.perform(post("/api/" + type).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
    }

    @ParameterizedTest
    @ValueSource(strings = {"http://", "https:///file.mp4", "http://:80/file", "https://?query=1",
            "https://example.com:65536/file", "https://example.com/file name"})
    void rejectsMalformedUrlsForVideosAndEpisodes(String url) throws Exception {
        String video = mapper.writeValueAsString(Map.of("title", "Видео", "url", url, "duration", 60));
        mvc.perform(post("/api/videos").contentType(MediaType.APPLICATION_JSON).content(video))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.url").exists());
        String podcast = mapper.writeValueAsString(Map.of("title", "Подкаст", "episodes", List.of(
                Map.of("number", 1, "title", "Эпизод", "audioUrl", url, "duration", 60))));
        mvc.perform(post("/api/podcasts").contentType(MediaType.APPLICATION_JSON).content(podcast))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors['episodes[0].audioUrl']").exists());
    }

    @Test
    void replacesEpisodesAndRemovesOrphans() throws Exception {
        String first = "{\"title\":\"P\",\"episodes\":[{\"number\":1,\"title\":\"Old\",\"audioUrl\":\"https://example.com/old.mp3\",\"duration\":10}]}";
        String created = mvc.perform(post("/api/podcasts").contentType(MediaType.APPLICATION_JSON).content(first))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String url = "/api/podcasts/" + mapper.readTree(created).get("id").asLong();
        String replacement = first.replace("Old", "New").replace("old.mp3", "new.mp3");
        mvc.perform(put(url).contentType(MediaType.APPLICATION_JSON).content(replacement))
                .andExpect(status().isOk()).andExpect(jsonPath("$.episodes[0].title").value("New"));
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM episodes", Integer.class));
        mvc.perform(put(url).contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"P\",\"episodes\":[]}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.episodes").isEmpty());
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM episodes", Integer.class));
    }

    @Test
    void returnsUniformErrorsForInvalidJsonAndId() throws Exception {
        mvc.perform(post("/api/articles").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
        mvc.perform(get("/api/articles/abc"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
        mvc.perform(patch("/api/articles/1")).andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }

    @Test
    void exposesOpenApiAndSwagger() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/articles'].post").exists())
                .andExpect(jsonPath("$.paths['/api/videos/{id}'].put").exists())
                .andExpect(jsonPath("$.paths['/api/podcasts/{id}'].delete").exists());
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }
}
