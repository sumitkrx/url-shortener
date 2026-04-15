package com.urlshortener.model;

import java.time.LocalDateTime;

public record ShortenResponse (
        String shortCode,
        String shortUrl,
        String originalUrl,
        Long clickCount,
        LocalDateTime createAt,
        LocalDateTime expiresAt
) {
    public static ShortenResponse fromEntity(UrlMapping mapping, String baseUrl) {
        return new ShortenResponse(
                mapping.getShortCode(),
                baseUrl + "/" + mapping.getShortCode(),
                mapping.getOriginalUrl(),
                mapping.getClickCount(),
                mapping.getCreatedAt(),
                mapping.getExpiresAt()
        );
    }
}
