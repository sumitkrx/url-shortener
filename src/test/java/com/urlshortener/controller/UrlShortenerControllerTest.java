package com.urlshortener.controller;

import tools.jackson.databind.ObjectMapper;
import com.urlshortener.model.ShortenRequest;
import com.urlshortener.model.UrlMapping;
import com.urlshortener.repository.UrlMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UrlShortenerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UrlMappingRepository urlMappingRepository;

    @BeforeEach
    void setUp() {
        urlMappingRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/shorten creates a short URL")
    void shorten_returnsCreated() throws Exception {
        ShortenRequest request = new ShortenRequest("https://github.com", null,null);

        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").isNotEmpty())
                .andExpect(jsonPath("$.originalUrl").value("https://github.com"))
                .andExpect(jsonPath("$.clickCount").value(0));

        assertThat(urlMappingRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("POST /api/shorten with custom alias uses that alias")
    void shorten_withCustomAlias() throws Exception {
        ShortenRequest request = new ShortenRequest("https://github.com", "github",null);

        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").value("github"));
    }

    @Test
    @DisplayName("POST /api/shorten with duplicate alias returns 409")
    void shorten_duplicateAliasReturnsConflict() throws Exception {
        urlMappingRepository.save(new UrlMapping("taken", "https://example.com",null));

        ShortenRequest request = new ShortenRequest("https://github.com", "taken",null);

        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(containsString("already taken")));
    }

    @Test
    @DisplayName("POST /api/shorten with blank URL returns 400")
    void shorten_blankUrlReturnsBadRequest() throws Exception {
        ShortenRequest request = new ShortenRequest("", null,null);

        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isNotEmpty());
    }

    @Test
    @DisplayName("GET /api/urls returns all shortened URLs")
    void getAllUrls_returnsList() throws Exception {
        urlMappingRepository.save(new UrlMapping("abc123", "https://github.com",null));
        urlMappingRepository.save(new UrlMapping("xyz789", "https://google.com",null));

        mockMvc.perform(get("/api/urls"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].shortCode").isNotEmpty())
                .andExpect(jsonPath("$[1].shortCode").isNotEmpty());
    }

    @Test
    @DisplayName("GET /api/stats/{code} returns stats for existing code")
    void getStats_returnsStats() throws Exception {
        urlMappingRepository.save(new UrlMapping("abc123", "https://github.com",null));

        mockMvc.perform(get("/api/stats/abc123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value("abc123"))
                .andExpect(jsonPath("$.clickCount").value(0));
    }

    @Test
    @DisplayName("GET /api/stats/{code} returns 404 for unknown code")
    void getStats_unknownCodeReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/stats/unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/urls/{code} deletes existing URL")
    void delete_removesUrl() throws Exception {
        urlMappingRepository.save(new UrlMapping("abc123", "https://github.com",null));

        mockMvc.perform(delete("/api/urls/abc123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Deleted successfully"));

        assertThat(urlMappingRepository.count()).isZero();
    }

    @Test
    @DisplayName("DELETE /api/urls/{code} returns 404 for unknown code")
    void delete_unknownCodeReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/urls/unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /{shortCode} redirects to original URL")
    void redirect_sendsToOriginalUrl() throws Exception {
        urlMappingRepository.save(new UrlMapping("abc123", "https://github.com",null));

        mockMvc.perform(get("/abc123"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://github.com"));

        UrlMapping updated = urlMappingRepository.findByShortCode("abc123").get();
        assertThat(updated.getClickCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("GET /{shortCode} returns 404 for unknown code")
    void redirect_unknownCodeReturnsNotFound() throws Exception {
        mockMvc.perform(get("/unknown"))
                .andExpect(status().isNotFound());
    }
}