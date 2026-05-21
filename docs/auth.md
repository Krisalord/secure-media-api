# Authentication & Authorization Design

## 1. Overview

This project implements an authentication system using:

- JWT access tokens (short-lived, stateless)
- Refresh tokens (long-lived, opaque, server-controlled)
- MongoDB-backed refresh session storage
- Refresh token rotation and revoked-token reuse detection
- Rate-limited authentication endpoints

Design is optimized for security (mitigating consequences of token theft) and scalability (stateless access tokens)

---

## 2. Token Strategy

The system uses **two types of tokens**:

### Access Token
- Type: JWT
- Lifetime: short
- Used for: all authenticated API requests
- Storage: client-side (header)

### Refresh Token
- Type: opaque random string
- Lifetime: long
- Usage: issuing new access tokens
- Storage: HTTP-only cookie

---

## 3. Why Two Tokens?

Using a single token leads to tradeoffs:

1) If it's long-lived, the security risk is high in case token is stolen
2) If it's short-lived, user experience is suboptimal due to frequent logins required

Solution:
- Short-lived access token → limits damage
- Long-lived refresh token → keeps user logged in

---


## 4. Password Handling

Passwords are:
- hashed using bcrypt
- never stored in plain text

Verification is done by comparing hashes.

---

## 5. Data Models

### Users Collection

```json
{
  "_id": "ObjectId",
  "email": "user@example.com",
  "passwordHash": "...",
  "role": "USER",
  "createdAt": "...",
  "updatedAt": "..."
}
```

`email` is unique

### Refresh Sessions Collection

```json
{
  "_id": "ObjectId",
  "userId": "String",
  "refreshTokenHash": "...",
  "expiresAt": "Instant",
  "createdAt": "Instant",
  "revokedAt": "Instant | null",
  "userAgent": "...",
  "ipAddress": "..."
}
```

`refreshTokenHash` is unique (prevent duplicate of active tokens)
`(userId, revokedAt, expiresAt)` is compound index (for active session lookup)
`expiresAt` TTL index (automatically removes expired sessions)


## 6. Authentication flows

### 6.1. Register

`POST /api/v1/auth/register`

```json
{
  "email": "newuser@email.address",
  "password": "rawPassword1"
}
```

1) Credentials are validated
2) Password is hashed
3) User is created and stored in MongoDB
4) Returns `201 Created`

### 6.2. Login

`POST /api/v1/auth/login`

```json
{
  "email": "newuser@email.address",
  "password": "rawPassword1"
}
```

1) Credentials are validated
2) User is fetched from MongoDB
3) Password is compared against hash
4) Refresh token is generated
5) Refresh session is created and stored in MongoDB
6) Refresh token is returned as an HTTP-only cookie
7) Access token is generated and returned in the response body

### 6.3. Refresh token rotation

`POST /api/v1/auth/refresh`

required Cookie - refresh_token=<opaque_token>

1) Refresh token is read from the cookie
2) Matching refresh session is fetched from MongoDB
3) Session is validated
4) The current session is revoked
5) New refresh token is generated
6) A new refresh session is created and stored in MongoDB
7) Refresh token is returned as an HTTP-only cookie
8) Access token is generated and returned in the response body

### 6.4. Logout

`POST /api/v1/auth/logout`

Cookies (optional)\
`refresh_token=<opaque_token>`

1) Refresh token is read from the cookie
2) If present, then the matching refresh session is revoked
3) Clear cookie
4) Response is `204 No Content`

### 6.5. Logout all devices

`POST /api/v1/auth/logout-all`

```http request
Authorization: Bearer <access_token>
```

Cookies (optional)\
`refresh_token=<opaque_token>`

1) JWT is validated
2) userId is extracted from JWT
3) All refresh sessions for the user are revoked
4) Clear current cookies
5) Response is `204 No Content`

## 7. Security Considerations

### Refresh token
- Stored in HTTP-only cookie
- Stored in MongoDB as hash
- Rotated every use
- Re-use detection invalidates all sessions

### Rate limiting
- Global rate limit to prevent high-volume attacks
- IP-based limits to prevent brute-force attacks
- User-based limits to prevent abuse of features per account


## 8. Notes

- `/api/v1` is set up for Web-based clients, but is versioned for future expansion.
- Role management is not implemented yet.




