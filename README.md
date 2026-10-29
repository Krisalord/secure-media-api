# Secure Media API

A backend API to manage media collections, built with Kotlin, Ktor, and PostgreSQL.
Supports secure user authentication and full CRUD operations for media and AI suggestions.

## Live Showcase

Check out the live frontend application powered by this API, fully deployed on Render:
**[Secure Media Dashboard Showcase](https://secure-media-frontend.onrender.com)**

*(Note: Since this is hosted on a free Render tier, the backend may take ~50 seconds to spin up on the first request).*

## 1. Features

- User registration and login
- CRUD Media management
- AI-powered recommendations via Gemini
- Input sanitization and validation
- Centralized error handling
- Response caching with Caffeine (poster URLs, user lookups, watch history)
- Modular, maintainable architecture
- Comprehensive unit and integration tests

## 2. Security & Authentication

This API uses a high-security dual-token architecture to protect users from XSS and token theft:

* **Dual Tokens:** Login issues a short-lived JWT (15 min) for API access and a long-lived Refresh Token (7 days) stored in an opaque, HttpOnly cookie.
* **Smart Rotation:** Hitting the `/refresh` endpoint swaps the old cookie for a new cookie and a new JWT.
* **Intrusion Detection:** If a bad actor steals a Refresh Cookie and tries to use it after the real user already rotated it, the API detects the anomaly and nukes all active sessions for that user.
* **Rate Limiting:** Global, IP-based, and User-based rate limiting to prevent brute-force and DDoS attacks.

## 3. Tech Stack
* **Core:** Kotlin, Ktor Server & Client
* **Database:** PostgreSQL, Exposed ORM, HikariCP Connection Pooling
* **Caching:** Caffeine
* **AI Integration:** Google Generative Language API (Gemini)
* **External API:** TMDB (poster URL lookups)
* **Security:** BCrypt Password Hashing, HMAC SHA-256 Token Hashing
* **Testing:** JUnit 5, MockK, Ktor Test Host, Testcontainers

## 4. Quick Start

1. Clone & Configure
```bash
git clone https://github.com/krisalord/secure-media-api.git
cd secure-media-api
```

2. Set up .env

The application relies on environment variables for secure configuration. You must create a .env file in the root directory of the project. The application.conf file will automatically read from this file during local development.

Create a .env file and add the following configuration:

```hocon
PORT=8080
DB_URL=jdbc:postgresql://localhost:5432/auth_dev_db
DB_USER=dev_user
DB_PASSWORD=dev_password
JWT_SECRET=my-super-secret-local-jwt-key
JWT_ISSUER=io.github.krisalord
JWT_AUDIENCE=secure-media-api
TOKEN_PEPPER=my-super-secret-local-pepper
FRONTEND_URL=http://localhost:5173
GEMINI_API_KEY=your_gemini_api_key_here
TMDB_API_KEY=your_tmdb_api_key_here
```

3. Boot the database
```bash
docker-compose up -d
```

4. Run the application
```bash
./gradlew run
```

## 5. API Reference

Interactive API documentation is available via **Swagger UI** at `/swagger` when the server is running.

The OpenAPI spec (`openapi.yaml`) covers all endpoints, request/response schemas, and authentication details.

## 6. Testing

Run unit tests:
```bash
./gradlew test --tests "io.github.krisalord.unit.*"
```

Run integration tests (requires Docker):
```bash
./gradlew test --tests "io.github.krisalord.integration.*"
```