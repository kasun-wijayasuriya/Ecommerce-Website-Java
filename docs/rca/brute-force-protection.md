# Root Cause Analysis — No Brute-Force Protection

## Vulnerability Overview

| Attribute | Details |
|-----------|---------|
| **Vulnerability** | No Brute-Force Protection on Login Endpoint |
| **Severity** | High |
| **CWE** | CWE-307 — Improper Restriction of Excessive Authentication Attempts |
| **OWASP** | A07:2021 — Identification and Authentication Failures |
| **Affected Endpoint** | `POST /api/auth/login` |
| **Affected Service** | `AuthService.java` |

---

## Description

The login endpoint (`POST /api/auth/login`) has no mechanism to limit the number of authentication attempts. An attacker can send unlimited login requests to this endpoint without any throttling, account lockout, CAPTCHA challenge, or rate limiting.

Each request flows through the following path:
1. Passes through `JwtAuthenticationFilter` (no token required for `/api/auth/**`)
2. Reaches `AuthController.login()`
3. Delegates to `AuthService.login()`
4. Calls `AuthenticationManager.authenticate()` with email and password
5. Either succeeds (returns JWT) or throws `BadCredentialsException` (returns generic error)

There is **nothing** to slow down, block, or detect a dictionary or brute-force attack.

---

## Risk Assessment

### Impact

| Impact | Description |
|--------|-------------|
| **Account Takeover** | An attacker can systematically try passwords against any user account until successful authentication |
| **Credential Stuffing** | Automated attacks using breached credential pairs from other services |
| **Resource Exhaustion** | Unlimited authentication requests can consume server resources (CPU, database connections) |
| **User Enumeration** | Response timing differences or error messages may reveal whether an email exists in the system |

### Likelihood

| Factor | Assessment |
|--------|------------|
| **Attack Complexity** | Low — requires only a script sending POST requests |
| **Prerequisites** | None — the endpoint is publicly accessible |
| **Automation** | Trivial — tools like Hydra, Burp Suite Intruder, or custom scripts |

### Risk Rating: **HIGH**

Combined with the **Weak Password Policy** (Vulnerability #4 — only 6 character minimum, no complexity), the effective keyspace for brute-force attacks is significantly reduced, making this vulnerability even more exploitable.

---

## Root Cause Analysis

### Code Evidence

**File:** `backend/src/main/java/com/ecommerce/service/AuthService.java`

```java
public AuthResponse login(LoginRequest request) {
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
    );

    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new BadRequestException("User not found"));

    String token = jwtTokenProvider.generateToken(authentication);
    return AuthResponse.of(token, UserResponse.fromUser(user));
}
```

**Findings:**
1. **No attempt tracking** — There is no counter, storage, or record of failed login attempts per user or per IP.
2. **No lockout mechanism** — The `User` model has no `failedAttempts`, `lockedUntil`, or similar fields.
3. **No rate limiting** — No filter, middleware, or annotation restricts request frequency.
4. **No CAPTCHA** — No challenge mechanism after failed attempts.
5. **No exponential backoff** — No increasing delay after consecutive failures.

### Grep Verification

A search for `rate.?limit|brute.?force|lockout|attempt|throttle|captcha` across the entire codebase returned **zero matches**, confirming no brute-force protection exists anywhere in the application.

---

## References

- **CWE-307:** https://cwe.mitre.org/data/definitions/307.html
- **OWASP Authentication Cheatsheet:** https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html
- **NIST SP 800-63B (Section 5.2.2):** https://pages.nist.gov/800-63-3/sp800-63b.html#memsecretver
