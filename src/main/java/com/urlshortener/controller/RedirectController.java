package com.urlshortener.controller;

import com.urlshortener.service.UrlShortenerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URI;

@Controller
@RequiredArgsConstructor
public class RedirectController {
    private final UrlShortenerService urlShortenerService;
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        return urlShortenerService.resolveAndTrack(shortCode)
                .map(originalUrl -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setLocation(URI.create(originalUrl));
                    return new ResponseEntity<Void>(headers, HttpStatus.FOUND);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
