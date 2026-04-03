# OAuth2 / OpenID Connect — Design Document

## Overview

This document describes the OAuth 2.0 / OpenID Connect implementation for Google Sign-In in the e-commerce application.

## Chosen Flow: Authorization Code Grant

The **Authorization Code Grant** flow is used, handled entirely by Spring Security OAuth2 Client.

### Flow Diagram

```
User clicks "Sign in with Google"
         │
         ▼
Browser redirects to Google Authorization Server
         │
         ▼
User authenticates with Google
         │
         ▼
Google redirects back with authorization code
         │
         ▼
Spring Security exchanges code for tokens (backend)
         │
         ▼
Backend extracts user info (email, name, picture)
         │
         ▼
Backend finds or creates user in MongoDB
         │
         ▼
Backend generates JWT, sets as httpOnly cookie
         │
         ▼
User is redirected to application home (authenticated)
```

## Security Requirements

| Requirement | Implementation |
|-------------|----------------|
| State parameter validation | Handled automatically by Spring Security OAuth2 |
| Token validation | Spring Security validates ID token signature, issuer, audience |
| User provisioning | Find existing user by email or create new one with USER role |
| JWT delivery | Same httpOnly Secure SameSite=Strict cookie as regular login |
| Client credentials | Loaded from environment variables (GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET) |

## User Provisioning Logic

1. Extract email, name, and picture from Google ID token
2. Look up user by email in MongoDB
3. If found → use existing account
4. If not found → create new account with:
    - Email from Google
    - First/last name parsed from Google name field
    - Profile image from Google picture URL
    - Role: USER
    - Active: true
5. Generate JWT and set as httpOnly cookie

## Configuration

Google OAuth2 credentials are configured via environment variables:
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`

Redirect URI: `{baseUrl}/login/oauth2/code/google`

Scopes: `openid`, `email`, `profile`
