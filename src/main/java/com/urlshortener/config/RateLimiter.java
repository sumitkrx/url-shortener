package com.urlshortener.config;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class RateLimiter {
    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_MILLIS = 60_000;

    private final ConcurrentHashMap<String, ConcurrentLinkedDeque<Long>> requestLog
            = new ConcurrentHashMap<>();

    public boolean isAllowed(String clientIP){
        long now = System.currentTimeMillis();
        long windowStart = now - WINDOW_MILLIS;

        ConcurrentLinkedDeque<Long> timestamps = requestLog
                .computeIfAbsent(clientIP, k -> new ConcurrentLinkedDeque<>());

        while(!timestamps.isEmpty() && timestamps.peekFirst() < windowStart){
            timestamps.pollFirst();
        }

        if(timestamps.size() >= MAX_REQUESTS){
            return false;
        }

        timestamps.addLast(now);
        return true;
    }

    public int getRemainingRequests(String clientIP){
        ConcurrentLinkedDeque<Long> timestamps = requestLog.get(clientIP);
        if(timestamps == null) return MAX_REQUESTS;

        long windowStart = System.currentTimeMillis() - WINDOW_MILLIS;
        while(!timestamps.isEmpty() && timestamps.peekFirst() < windowStart){
            timestamps.pollFirst();
        }

        return Math.max(0, MAX_REQUESTS - timestamps.size());
    }
}
