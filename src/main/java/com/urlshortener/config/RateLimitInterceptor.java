package com.urlshortener.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiter rateLimiter;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        String clientIP = getClientIp(request);

        int remaining = rateLimiter.getRemainingRequests(clientIP);
        response.setHeader("X-RateLimit-Limit","10");
        response.setHeader("X-RateLimit-Remaining",String.valueOf(remaining));

        if(!rateLimiter.isAllowed(clientIP)){
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("error","Rate limit exceeded. Try again in few minutes."));
            return false;
        }
        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if(forwardedFor != null && !forwardedFor.isEmpty()){
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
