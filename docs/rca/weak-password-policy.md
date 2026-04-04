# Root Cause Analysis — Weak Password Policy

## Vulnerability Overview

| Attribute | Details |
|-----------|---------|
| **Vulnerability** | Weak Password Policy — No Complexity Requirements |
| **Severity** | Medium |
| **CWE** | CWE-521 — Weak Password Requirements |
| **OWASP** | A07:2021 — Identification and Authentication Failures |
| **Affected Endpoint** | `POST /api/auth/register` |
| **Affected Component** | `RegisterRequest.java`, `AuthService.java` |

---

## Description

The application enforces only a minimum password length of 6 characters during user registration. There are no requirements for password complexity such as uppercase letters, lowercase letters, digits, or special characters. This allows users to set trivially weak passwords that are easily guessable through dictionary attacks or brute-force.

The only validation constraint on the password field is:
```java
@Size(min = 6, message = "Password must be at least 6 characters")
private String password;
```

---

## Risk Assessment

### Impact

| Impact | Description |
|--------|-------------|
| **Account Compromise** | Weak passwords like `123456`, `password`, `abcdef` are easily guessed by attackers |
| **Credential Stuffing** | Common passwords from breached databases are likely to succeed |
| **Brute-Force Efficiency** | A 6-character lowercase-only password has only 26^6 = ~308 million combinations, trivially crackable |

### Likelihood

| Factor | Assessment |
|--------|------------|
| **Attack Complexity** | Low — dictionary attacks using common password lists are highly effective |
| **Prerequisites** | Access to the login endpoint (publicly accessible) |
| **Automation** | Trivial — automated tools can test thousands of common passwords per second |

### Risk Rating: **MEDIUM**

While the risk is Medium on its own, combined with the **No Brute-Force Protection** (Vulnerability #1), the effective risk becomes significantly higher since attackers can test unlimited attempts against accounts with weak passwords.

---

## Root Cause Analysis

### Code Evidence

**File:** `backend/src/main/java/com/ecommerce/dto/request/RegisterRequest.java`

```java
@NotBlank(message = "Password is required")
@Size(min = 6, message = "Password must be at least 6 characters")
private String password;
```

**File:** `backend/src/main/java/com/ecommerce/service/AuthService.java`

```java
public AuthResponse register(RegisterRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
        throw new BadRequestException("Email already registered");
    }

    User user = User.builder()
            .password(passwordEncoder.encode(request.getPassword()))
            // ... no password strength validation
            .build();
    // ...
}
```

**Findings:**
1. **Minimum length only** — The only constraint is `@Size(min = 6)`, which accepts passwords as short as 6 characters.
2. **No complexity requirements** — No uppercase, lowercase, digit, or special character requirements.
3. **No breached password check** — The application does not check the password against known breached password lists.
4. **No frontend strength indicator** — The registration form does not provide real-time feedback on password strength.

---

## References

- **CWE-521:** https://cwe.mitre.org/data/definitions/521.html
- **NIST SP 800-63B (Section 5.1.1.2):** https://pages.nist.gov/800-63-3/sp800-63b.html#sec5
- **OWASP Authentication Cheatsheet:** https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html
