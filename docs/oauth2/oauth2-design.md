# OAuth2 / OpenID Connect — Design Document

## Overview

This document describes the OAuth 2.0 / OpenID Connect implementation for Google Sign-In in the e-commerce application.

## Chosen Flow: Authorization Code Grant with PKCE

The **Authorization Code Grant with PKCE** flow is used, handled entirely by Spring Security OAuth2 Client.

### Flow Diagram

```
User clicks "Sign in with Google" (frontend /login page)
         │
         ▼
Browser redirects to Google Authorization Server
         │
         ▼
User authenticates with Google
         │
         ▼
Google redirects back with authorization code
         │  (to http://localhost:8080/login/oauth2/code/google)
         ▼
Spring Security exchanges code for tokens (backend)
         │
         ▼
OAuth2LoginSuccessHandler extracts user info (email, name, picture)
         │
         ▼
Backend finds or creates user in MongoDB
         │  (OAuth users get BCrypt-hashed random password)
         ▼
Backend generates JWT, sets as httpOnly cookie
         │  (secure=false for localhost, secure=true for production)
         │  (sameSite=Lax to allow OAuth redirect flow)
         ▼
Backend redirects to frontend /oauth-success
         │
         ▼
Frontend fetches user profile, updates auth state
         │
         ▼
Frontend redirects to home page (authenticated, username visible)
```

## Security Requirements

| Requirement | Implementation |
|-------------|----------------|
| State parameter validation | Handled automatically by Spring Security OAuth2 |
| Token validation | Spring Security validates ID token signature, issuer, audience |
| User provisioning | Find existing user by email or create new one with USER role |
| JWT delivery | httpOnly cookie with dynamic secure flag (false for localhost, true for production) |
| Cookie SameSite | `Lax` (required to allow OAuth redirect flow) |
| Client credentials | Loaded from environment variables (GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET) |
| Password for OAuth users | BCrypt-hashed random UUID (required by Spring Security UserDetails interface) |

## User Provisioning Logic

1. Extract email, name, and picture from Google OAuth2 user info
2. Look up user by email in MongoDB
3. If found → use existing account
4. If not found → create new account with:
    - Email from Google
    - First/last name parsed from Google name field (split on space)
    - Profile image from Google picture URL
    - **Password**: BCrypt-hashed random UUID (`UUID.randomUUID()`)
    - Role: USER
    - Active: true
5. Generate JWT token for user email
6. Set JWT as httpOnly cookie with appropriate security settings
7. Redirect to frontend `/oauth-success` callback

### Why OAuth Users Have Passwords

Spring Security's `UserDetails` interface **requires** a non-null password field. OAuth users are assigned a randomly generated, BCrypt-hashed password because:

- The password is **never used for authentication** (OAuth users always authenticate via Google)
- Random UUID ensures cryptographic strength and uniqueness
- BCrypt hashing ensures the password is never stored in plain text
- This is **industry standard practice** (Auth0, Firebase Auth, ASP.NET Identity all do this)

## Cookie Configuration

The JWT cookie is configured dynamically based on the environment:

```java
String referer = request.getHeader("Referer");
boolean isLocalhost = referer != null && referer.contains("localhost");

ResponseCookie jwtCookie = ResponseCookie.from("jwt-token", token)
        .httpOnly(true)           // Prevents JavaScript access (XSS protection)
        .secure(!isLocalhost)     // true for production (HTTPS), false for localhost (HTTP)
        .path("/")                // Available across entire application
        .maxAge(jwtExpirationMs / 1000)
        .sameSite("Lax")          // Allows OAuth redirect flow
        .build();
```

### Cookie Attributes Explained

| Attribute | Value | Reason |
|-----------|-------|--------|
| `httpOnly` | `true` | Prevents JavaScript access, mitigating XSS token theft |
| `secure` | Dynamic | `false` for localhost (HTTP), `true` for production (HTTPS) |
| `sameSite` | `Lax` | Required for OAuth redirect flow (Strict would block it) |
| `path` | `/` | Cookie available across entire application |
| `maxAge` | From config | Matches JWT expiration time |

## Frontend OAuth Callback

After the backend sets the JWT cookie, it redirects to `http://localhost:5173/oauth-success`. This page:

1. Calls `GET /api/users/profile` (browser automatically sends JWT cookie)
2. Updates auth store with user data and `isAuthenticated: true`
3. Redirects to home page `/`
4. If profile fetch fails, shows error and redirects to login

The `Navbar` component calls `initializeAuth()` on mount to sync auth state with the backend JWT cookie, ensuring the username appears after page refresh or OAuth redirect.

## Configuration

Google OAuth2 credentials are configured via environment variables:
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`

### application.yml

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: openid,email,profile
            redirect-uri: "{baseUrl}/login/oauth2/code/google"
            authorization-grant-type: authorization_code
            client-name: Google
        provider:
          google:
            authorization-uri: https://accounts.google.com/o/oauth2/v2/auth
            token-uri: https://oauth2.googleapis.com/token
            user-info-uri: https://www.googleapis.com/oauth2/v3/userinfo
            user-name-attribute: sub
```

### Google Cloud Console Setup

**Authorized redirect URI** (must be exactly):
```
http://localhost:8080/login/oauth2/code/google
```

**Scopes requested**:
- `openid` - OpenID Connect (required for OIDC)
- `email` - User's email address
- `profile` - User's name and profile picture

## Implementation Files

### Backend
| File | Purpose |
|------|---------|
| `OAuth2LoginSuccessHandler.java` | Handles OAuth success, creates users, sets JWT cookie |
| `SecurityConfig.java` | Enables OAuth2 login, configures success/failure handlers |
| `application.yml` | Google OAuth2 client configuration |

### Frontend
| File | Purpose |
|------|---------|
| `OAuthSuccess.jsx` | OAuth callback page, fetches profile, redirects to home |
| `Login.jsx` | Contains "Sign in with Google" button |
| `authStore.js` | `initializeAuth()` and `fetchProfile()` for auth state sync |
| `Navbar.jsx` | Calls `initializeAuth()` on mount to detect JWT cookie |
