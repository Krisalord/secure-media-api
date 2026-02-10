# Secure Media API

Media collections API built with Kotlin, Ktor & MongoDB.

## 1 Features

- User registration and login
- CRUD Media management
- AI-powered media recommendations
- Input sanitization and validation
- Centralized error handling
- Modular, maintainable architecture
- Unit and API route testing with MockK

## 2 Tech Stack

- **Language & Framework:** Kotlin, Ktor
- **Database:** MongoDB (KMongo)
- **AI Service:** OpenAI API
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

    openai {
        apiKey = "YOUR_OPENAI_API_KEY"
    }
}
```
3. Run
```bash
  /gradlew run
```
API runs on `http://localhost:8080`

## 4 Configuration

The app requires:

- `ktor.mongo.uri` — MongoDB connection string
- `ktor.mongo.database` — Mongo database name
- `ktor.jwt.secret` — Replace with strong secret

Optional: (required for AI suggestion to work)

- `ktor.openai.apiKey` — OpenAI API key for AI suggestion

## 5 Example Requests

### 1 Register
Request
```bash
POST /register
Content-Type: application/json
{
  "email": "user@example.com",
  "passwordBeforeHash": "securePass123"
}
```
Response
```bash
{
  "id": "user_id_1",
  "email": "user@example.com"
}
```

### 2 Login
Request
```bash
POST /login
Content-Type: application/json
{
  "email": "user@example.com",
  "passwordBeforeHash": "securePass123"
}
```
Response
```bash
{
  "token": "jwt_token_here",
}
```

### 3 Create Media
Request
```bash
POST /media
Content-Type: application/json
Authorization: Bearer JWT_TOKEN_HERE
{
  "title": "Movie 1",
  "genres": ["ACTION", "ADVENTURE"],
  "rating": 9,
  "status": "COMPLETED"
}
```
Response
```bash
{
  "id": "media_id_1",
  "title": "Movie 1",
  "genres": ["ACTION", "ADVENTURE"],
  "rating": 9,
  "status": "COMPLETED"
}
```

### 4 Get All Media
Request
```bash
GET /media
Authorization: Bearer JWT_TOKEN_HERE
```
Response
```bash
[
  {
    "id": "media_id_1",
    "title": "Movie 1",
    "genres": ["ACTION", "ADVENTURE"],
    "rating": 9,
    "status": "COMPLETED"
  },
  {
    "id": "media_id_2",
    "title": "Movie 2",
    "genres": ["DRAMA"],
    "rating": 7,
    "status": "WATCHING"
  }
]
```

### 5 Get Single Media
Request
```bash
GET /media/media_id_1
Authorization: Bearer JWT_TOKEN_HERE
```
Response
```bash
{
    "id": "media_id_1",
    "title": "Movie 1",
    "genres": ["ACTION", "ADVENTURE"],
    "rating": 9,
    "status": "COMPLETED"
}
```

### 6 Update Media
Request
```bash
PUT /media/media_id_1
Authorization: Bearer JWT_TOKEN_HERE
Content-Type: application/json
{
  "title": "Movie 1 [edited]",
  "genres": ["ACTION", "SCI-FI"],
  "rating": 10,
  "status": "COMPLETED"
}
```
Response
```bash
HTTP 204 No Content
```


### 7 Delete Media
Request
```bash
DELETE /media/media_id_1
Authorization: Bearer JWT_TOKEN_HERE
```
Response
```bash
HTTP 204 No Content
```

### 8 Suggest new media with AI
Request
```bash
POST /ai/suggest
Authorization: Bearer JWT_TOKEN_HERE
```
Response
```bash
{
  "suggestion": "Based on your watch history i can recommend these movies/shows: {recommendations}"
}
```