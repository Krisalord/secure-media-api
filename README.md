# Secure Media API

A backend API to manage media collections, built with Kotlin, Ktor, and PostgreSQL.
Supports secure user authentication and full CRUD operations for media and AI suggestions.

## 1 Features

- User registration and login
- CRUD Media management
- Input sanitization and validation
- Centralized error handling
- Modular, maintainable architecture
- Unit and API route testing with MockK

## 2. Security & Authentication

This API uses a high-security dual-token architecture to protect users from XSS and token theft:

* **Dual Tokens:** Login issues a short-lived JWT (15 min) for API access and a long-lived Refresh Token (7 days) stored in an opaque, HttpOnly cookie.
* **Smart Rotation:** Hitting the `/refresh` endpoint swaps the old cookie for a new cookie and a new JWT.
* **Intrusion Detection:** If a bad actor steals a Refresh Cookie and tries to use it after the real user already rotated it, the API detects the anomaly and nukes all active sessions for that user.
* **Rate Limiting:** Global, IP-based, and User-based rate limiting to prevent brute-force and DDoS attacks.

## 3. Tech Stack
* **Core:** Kotlin, Ktor Server & Client
* **Database:** PostgreSQL, Exposed ORM, HikariCP Connection Pooling
* **AI Integration:** Google Generative Language API (Gemini)
* **Security:** BCrypt Password Hashing, HMAC SHA-256 Token Hashing
* **Testing:** JUnit 5, MockK, Ktor Test Host, Testcontainers

## 4 Quick Start

1. Clone & Configure
```bash
  git clone https://github.com/krisalord/secure-media-api.git
  cd secure-media-api
```

2. Set up .env

The application relies on environment variables for secure configuration. You must create a .env file in the root directory of the project. The application.conf file will automatically read from this file during local development.

Create a .env file and add the following configuration:

`.conf` files are gitignored. You must create `src/main/resources/application.conf` manually.

``` hocon 
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
```

3. Boot the database
```
docker-compose up -d
```

4. Run the application
```bash
  /gradlew run
```

## 5 API Reference

1. Authentication endpoints

| Method   | Endpoint                | Description                       | Auth Required        |
|----------|-------------------------|-----------------------------------|----------------------|
| POST     | /api/v1/auth/register   | Register a new user               | No                   |
| POST     | /api/v1/auth/login      | Authenticate & receive tokens     | No                   |
| POST     | /api/v1/auth/refresh    | Refresh JWT using HttpOnly Cookie | No (Requires Cookie) |
| POST     | /api/v1/auth/logout     | Log out of the current device     | No (Requires Cookie) |
| POST     | /api/v1/auth/logout-all | Revoke all active devices         | Yes (Bearer JWT)     |

2. Media endpoints

| Method | Endpoint           | Description                       | Auth Required     |
|--------|--------------------|-----------------------------------|-------------------|
| POST   | /api/v1/media      | Log a watched movie or TV show    | Yes (Bearer JWT)  |
| GET    | /api/v1/media      | Retrieve user's watch history     | Yes (Bearer JWT)  |
| DELETE | /api/v1/media/{id} | Delete a media entry from history | Yes (Bearer JWT)  |

3. AI Recommendation endpoints

| Method | Endpoint                | Description                                  | Auth Required    |
|--------|-------------------------|----------------------------------------------|------------------|
| POST   | /api/v1/recommendations | Generate AI recommendations based on history | Yes (Bearer JWT) |