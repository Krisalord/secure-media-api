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

## 2 Security & Authentication
This API uses a high-security dual-token architecture to protect users from XSS and token theft:

Dual Tokens: Login issues a short-lived JWT (15 min) for API access and a long-lived Refresh Token (7 days) stored in an opaque, HttpOnly cookie.

Smart Rotation: Hitting the /refresh endpoint swaps the old cookie for a new cookie and a new JWT.

Intrusion Detection: If a bad actor steals a Refresh Cookie and tries to use it after the real user already rotated it, the API detects the anomaly and nukes all active sessions for that user.

Rate Limiting: Global, IP-based, and User-based rate limiting to prevent brute-force and DDoS attacks.

## 3 Tech Stack

Core: Kotlin, Ktor Server & Client

Database: PostgreSQL, Exposed ORM, HikariCP Connection Pooling

AI Integration: Google Generative Language API (Gemini)

Security: BCrypt Password Hashing, HMAC SHA-256 Token Hashing

Testing: JUnit 5, MockK, Ktor Test Host, Testcontainers

## 4 Quick Start

1. Clone & Configure
```bash
  git clone https://github.com/krisalord/secure-media-api.git
  cd secure-media-api
```

2. Create your config file

`.conf` files are gitignored. You must create `src/main/resources/application.conf` manually.

``` hocon 
ktor {
  application {
    modules = [ io.github.krisalord.ApplicationKt.module ]
  }

  deployment {
    port = 8080
  }

  database {
    driverClassName = "org.postgresql.Driver"
    jdbcUrl = ""
    username = ""
    password = ""
    maximumPoolSize = 10
  }

  jwt {
    secret = "secret-jwt-signing-key"
    issuer = ""
    audience = "secure-media-api"
    accessValidityMs = 120000
  }

  refreshToken {
    validityDays: 7
    reuseDetectionEnabled: true
    maxSessionsPerUser: 5
    tokenHashPepper = "separate-secret-for-refresh-token-hashing"
  }

  cors {
    allowedHosts = [
      
    ]
  }

  cookies {
    secure = false
    sameSite = "Strict"
  }

  ai {
    geminiApiKey = "your gemini api key"
  }
}
```

3. Boot the database
```
docker-compose up -d
```

4. Run the application
```bash
  /gradlew run
```

API runs on `http://localhost:8080`






