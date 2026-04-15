package com.urlshortener.service;

import com.urlshortener.model.UrlMapping;
import com.urlshortener.repository.UrlMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlShortenerServiceTest {

    @Mock
    private UrlMappingRepository urlMappingRepository;

    @InjectMocks
    private UrlShortenerService urlShortenerService;

    private UrlMapping sampleMapping;

    @BeforeEach
    void setUp() {
        sampleMapping = new UrlMapping("abc123", "https://github.com", null);
        sampleMapping.setId(1L);
    }

    @Test
    @DisplayName("shortenUrl generates a short code and saves to database")
    void shortenUrl_savesAndReturnsMapping() {
        when(urlMappingRepository.existsByShortCode(anyString())).thenReturn(false);
        when(urlMappingRepository.save(any(UrlMapping.class))).thenReturn(sampleMapping);

        UrlMapping result = urlShortenerService.shortenUrl("https://github.com", null,null);

        assertThat(result).isNotNull();
        assertThat(result.getOriginalUrl()).isEqualTo("https://github.com");
        verify(urlMappingRepository, times(1)).save(any(UrlMapping.class));
    }

    @Test
    @DisplayName("shortenUrl adds https:// when protocol is missing")
    void shortenUrl_normalizesUrl() {
        when(urlMappingRepository.existsByShortCode(anyString())).thenReturn(false);
        when(urlMappingRepository.save(any(UrlMapping.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UrlMapping result = urlShortenerService.shortenUrl("github.com", null,null);

        assertThat(result.getOriginalUrl()).isEqualTo("https://github.com");
    }

    @Test
    @DisplayName("shortenUrl uses custom alias when provided")
    void shortenUrl_usesCustomAlias() {
        when(urlMappingRepository.existsByShortCode("myalias")).thenReturn(false);
        when(urlMappingRepository.save(any(UrlMapping.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UrlMapping result = urlShortenerService.shortenUrl("https://github.com", "myalias",null);

        assertThat(result.getShortCode()).isEqualTo("myalias");
    }

    @Test
    @DisplayName("shortenUrl throws when custom alias is already taken")
    void shortenUrl_rejectsDuplicateAlias() {
        when(urlMappingRepository.existsByShortCode("taken")).thenReturn(true);

        assertThatThrownBy(() -> urlShortenerService.shortenUrl("https://github.com", "taken",null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already taken");
    }

    @Test
    @DisplayName("resolveAndTrack returns URL and increments click count")
    void resolveAndTrack_returnsUrlAndTracksClick() {
        when(urlMappingRepository.findByShortCode("abc123"))
                .thenReturn(Optional.of(sampleMapping));
        when(urlMappingRepository.save(any())).thenReturn(sampleMapping);

        Optional<String> result = urlShortenerService.resolveAndTrack("abc123");

        assertThat(result).isPresent().contains("https://github.com");
        assertThat(sampleMapping.getClickCount()).isEqualTo(1L);
        verify(urlMappingRepository).save(sampleMapping);
    }

    @Test
    @DisplayName("resolveAndTrack returns empty for unknown short code")
    void resolveAndTrack_returnsEmptyForUnknownCode() {
        when(urlMappingRepository.findByShortCode("unknown"))
                .thenReturn(Optional.empty());

        Optional<String> result = urlShortenerService.resolveAndTrack("unknown");

        assertThat(result).isEmpty();
        verify(urlMappingRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteByShortCode returns true when found and deleted")
    void deleteByShortCode_returnsTrueWhenFound() {
        when(urlMappingRepository.findByShortCode("abc123"))
                .thenReturn(Optional.of(sampleMapping));

        boolean result = urlShortenerService.deleteByShortCode("abc123");

        assertThat(result).isTrue();
        verify(urlMappingRepository).delete(sampleMapping);
    }

    @Test
    @DisplayName("deleteByShortCode returns false when not found")
    void deleteByShortCode_returnsFalseWhenNotFound() {
        when(urlMappingRepository.findByShortCode("unknown"))
                .thenReturn(Optional.empty());

        boolean result = urlShortenerService.deleteByShortCode("unknown");

        assertThat(result).isFalse();
        verify(urlMappingRepository, never()).delete(any());
    }
}