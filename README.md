# URL Shortener

A URL shortener built from scratch with Java 17 and Spring Boot 3.

## Tech Stack

- **Java 17** + **Spring Boot 3.2**
- **Spring Data JPA** with **H2** in-memory database
- **Thymeleaf** for the web UI
- **Lombok** for boilerplate reduction

## Features

- Shorten long URLs with random 6-character Base62 codes
- Custom aliases (3–10 alphanumeric characters)
- Click tracking on every redirect
- IP-based rate limiting (10 requests/minute sliding window)
- Input validation and global error handling

## Getting Started

```bash
cd url-shortener
mvn spring-boot:run
```

Open `http://localhost:8080` for the web UI.

Database console available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:urlshortener`, user: `sa`, no password).

## API

**Shorten a URL**

```bash
curl -X POST http://localhost:8080/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://example.com/long-path"}'
```

**Shorten with a custom alias**

```bash
curl -X POST http://localhost:8080/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://example.com", "customAlias": "mylink"}'
```

**List all URLs** — `GET /api/urls`

**Get stats** — `GET /api/stats/{shortCode}`

**Delete** — `DELETE /api/urls/{shortCode}`

**Redirect** — `GET /{shortCode}` → 302 to original URL

## Project Structure

```
src/main/java/com/urlshortener/
├── UrlShortenerApplication.java    — Entry point
├── model/
│   ├── UrlMapping.java             — JPA entity
│   ├── ShortenRequest.java         — Request DTO (Java Record)
│   └── ShortenResponse.java        — Response DTO (Java Record)
├── repository/
│   └── UrlMappingRepository.java   — Data access layer
├── service/
│   └── UrlShortenerService.java    — Business logic
├── controller/
│   ├── UrlShortenerController.java — REST API
│   ├── RedirectController.java     — URL redirects
│   └── PageController.java         — Serves the web UI
└── config/
    ├── GlobalExceptionHandler.java — Centralized error handling
    ├── RateLimiter.java            — Sliding window rate limiter
    ├── RateLimitInterceptor.java   — HTTP interceptor
    └── WebConfig.java              — Registers the interceptor
```

## Rate Limiting

API endpoints under `/api/**` are limited to 10 requests per minute per IP. Responses include `X-RateLimit-Limit` and `X-RateLimit-Remaining` headers. Exceeding the limit returns `429 Too Many Requests`.