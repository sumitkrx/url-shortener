package com.urlshortener.service;

import com.urlshortener.model.UrlMapping;
import com.urlshortener.repository.UrlMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UrlShortenerService {
    private final UrlMappingRepository urlMappingRepository;
    private static final String CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int SHORT_CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Transactional
    public UrlMapping shortenUrl(String originalUrl){
        String normalizedUrl = normalizedUrl(originalUrl);
        String shortCode = generateUniqueShortCode();
        UrlMapping mapping = new UrlMapping(shortCode, normalizedUrl);
        return urlMappingRepository.save(mapping);
    }

    @Transactional
    public Optional<String> resolveAndTrack(String shortCode){
        return urlMappingRepository.findByShortCode(shortCode)
                .map(mapping -> {
                    mapping.incrementClickCount();
                    urlMappingRepository.save(mapping);
                    return mapping.getOriginalUrl();
                });
    }

    @Transactional(readOnly = true)
    public List<UrlMapping> getAllMappings(){
        return urlMappingRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<UrlMapping> getStats(String shortCode){
        return urlMappingRepository.findByShortCode(shortCode);
    }

    @Transactional
    public boolean deleteByShortCode(String shortCode){
        return urlMappingRepository.findByShortCode(shortCode)
                .map(mapping -> {
                    urlMappingRepository.delete(mapping);
                    return true;
                }).orElse(false);
    }

    private String generateUniqueShortCode(){
        String shortCode = "";
        do {
            shortCode = generateRandomCode();
        } while(urlMappingRepository.existsByShortCode(shortCode));
        return shortCode;
    }

    private String generateRandomCode(){
        StringBuilder sb = new StringBuilder(SHORT_CODE_LENGTH);
        for(int i = 0; i < SHORT_CODE_LENGTH; i++){
            int randomIndex = RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }

    private String normalizedUrl(String url){
        String trimmed = url.trim();
        if(!trimmed.startsWith("http://") &&  !trimmed.startsWith("https://")){
            return "https://" + trimmed;
        }
        return trimmed;
    }
}
