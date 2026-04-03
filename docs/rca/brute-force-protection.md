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
3. **No rate limiting** — No Spring Cloud Gateway rate limiter, no Bucket4j, no filter-based throttling.
4. **No CAPTCHA** — No reCAPTCHA, hCaptcha, or similar challenge after failed attempts.
5. **No exponential backoff** — No increasing delay after consecutive failures.
6. **No IP-based throttling** — `SecurityConfig` does not include any request-rate-limiting filter.

### Grep Verification

A search for `rate.?limit|brute.?force|lockout|attempt|throttle|captcha` across the entire codebase returned **zero matches**, confirming no brute-force protection exists anywhere in the application.

---

## Proposed Fix Strategy

### Approach: Redis-Based Account Lockout + IP Rate Limiting

#### Layer 1: Account-Level Lockout (Redis-Based)

| Parameter | Value | Rationale |
|-----------|-------|-----------|
| **Max Failed Attempts** | 5 | Balances security with user convenience |
| **Lockout Duration** | 15 minutes | Sufficient to deter automated attacks, not overly punitive for legitimate users |
| **Storage** | Redis (key: `login:attempts:{email}`) | Fast, TTL-supported, distributed-friendly |
| **Reset on Success** | Yes | Successful login clears the attempt counter |

**Logic:**
```
On failed login:
  1. Increment attempt counter for email in Redis
  2. Set TTL of 15 minutes on first attempt
  3. If attempts >= 5 → throw LockedException (HTTP 423)

On successful login:
  1. Delete attempt counter for email from Redis

Before authentication:
  1. Check if email is locked (attempts >= 5)
  2. If locked → reject immediately with 423
```

#### Layer 2: IP-Based Rate Limiting (Bucket4j)

| Parameter | Value | Rationale |
|-----------|-------|-----------|
| **Rate Limit** | 10 requests per minute per IP | Allows normal usage, blocks automated flooding |
| **Implementation** | Bucket4j in-memory bucket per IP | Lightweight, no external dependency beyond the library |
| **Response** | HTTP 429 (Too Many Requests) | Standard rate limit response |

#### Layer 3: Generic Error Messages (Already Implemented)

The existing `GlobalExceptionHandler` returns a generic "Invalid email or password" message for `BadCredentialsException`. This is **good practice** — it does not reveal whether the email exists or the password was wrong. **No changes needed here.**

---

## Files to Create/Modify

### New Files
| File | Purpose |
|------|---------|
| `service/LoginAttemptService.java` | Redis-based attempt counter with lockout logic |
| `exception/LockedException.java` | Custom exception with HTTP 423 status |
| `config/RateLimitConfig.java` | Bucket4j IP-based rate limiting filter |

### Modified Files
| File | Change |
|------|--------|
| `pom.xml` | Add `spring-boot-starter-data-redis` and `bucket4j-core` dependencies |
| `service/AuthService.java` | Add lockout check before authentication, increment/reset attempt counter |
| `exception/GlobalExceptionHandler.java` | Add handler for `LockedException` |
| `application.properties` | Add Redis connection configuration |

---

## Testing Strategy

### Unit Tests
1. **Lockout triggers after 5 failures** — Send 5 failed logins, verify 6th returns 423
2. **Successful login resets counter** — Send 3 failures, then 1 success, then 5 more failures — lockout should trigger on the 8th total (not the 6th)
3. **Lockout expires after TTL** — Send 5 failures, wait 15 minutes, verify next attempt is allowed
4. **Rate limit triggers after 10 requests/minute** — Send 11 requests from same IP, verify 11th returns 429

### Manual Testing
```bash
# Test 1: Brute-force simulation
for i in {1..10}; do
  curl -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"test@test.com","password":"wrong"}'
done
# Expected: First 5 return 401, 6th+ return 423

# Test 2: Rate limit simulation
for i in {1..15}; do
  curl -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"test@test.com","password":"wrong"}' &
done
# Expected: Some requests return 429
```

---

## References

- **CWE-307:** https://cwe.mitre.org/data/definitions/307.html
- **OWASP Authentication Cheatsheet:** https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html
- **NIST SP 800-63B (Section 5.2.2):** https://pages.nist.gov/800-63-3/sp800-63b.html#memsecretver
- **Bucket4j Documentation:** https://bucket4j.com/
