package com.urlshortener.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ShortenRequest (
    @NotBlank(message = "URL is required")
    @Size(max = 2048, message = "URL must be less than 2048 characters")
    String url
){}