package com.urlshortener.controller;

import com.urlshortener.model.ShortenRequest;
import com.urlshortener.model.ShortenResponse;
import com.urlshortener.model.UrlMapping;
import com.urlshortener.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UrlShortenerController {
    private final UrlShortenerService urlShortenerService;

    @PostMapping("/shorten")
    public ResponseEntity<ShortenResponse> shortenUrl (
        @Valid @RequestBody ShortenRequest request,
                HttpServletRequest httpRequest){

        UrlMapping mapping = urlShortenerService.shortenUrl(request.url());
        String baseUrl = getBaseUrl(httpRequest);
        ShortenResponse response = ShortenResponse.fromEntity(mapping,baseUrl);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/urls")
    public ResponseEntity<List<ShortenResponse>> getAllUrls(
            HttpServletRequest httpRequest){
        String baseUrl = getBaseUrl(httpRequest);
        List<ShortenResponse> responses = urlShortenerService.getAllMappings()
                .stream()
                .map(mapping -> ShortenResponse.fromEntity(mapping,baseUrl))
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/stats/{shortCode}")
    public ResponseEntity<ShortenResponse> getUrlStats(
            @PathVariable String shortCode,
            HttpServletRequest httpRequest){

        String baseUrl = getBaseUrl(httpRequest);
        return urlShortenerService.getStats(shortCode)
                .map(mapping -> ResponseEntity.ok(
                        ShortenResponse.fromEntity(mapping,baseUrl)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/urls/{shortCode}")
    public ResponseEntity<Map<String,String>> deleteUrl(
            @PathVariable String shortCode){
        boolean deleted = urlShortenerService.deleteByShortCode(shortCode);
        if(deleted){
            return ResponseEntity.ok(Map.of("message","Deleted successfully"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error","Short code not found"));
    }

    private String getBaseUrl(HttpServletRequest request){
        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();
        return scheme + "://" + host + ":" + port;
    }
}
