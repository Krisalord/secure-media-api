# Secure Media API

A backend API to manage media collections, built with Kotlin, Ktor, and MongoDB.
Supports secure user authentication and full CRUD operations for media.

## 1 Features

- User registration and login
- CRUD Media management
- Input sanitization and validation
- Centralized error handling
- Modular, maintainable architecture
- Unit and API route testing with MockK

## 2 Tech Stack

- **Language & Framework:** Kotlin, Ktor
- **Database:** MongoDB (KMongo)
- **Authentication:** JWT
- **Security & Validation:** Input validation, BCrypt

## 3 Quick Start

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

    mongo {
        uri = "YOUR_MONGO_URI"
        database = "secure-media-api"
    }

    jwt {
        secret = "YOUR_SECRET_KEY"
        issuer = "secure-media"
        audience = "secure-media-users"
        validityMs = 86400000
    }
}
```
3. Run
```bash
  /gradlew run
```
API runs on `http://localhost:8080`






