# Root Cause Analysis — JWT Exposure in Client-Side Storage

## Vulnerability Overview

| Attribute | Details |
|-----------|---------|
| **Vulnerability** | JWT Token Stored in Browser localStorage |
| **Severity** | High |
| **CWE** | CWE-922 — Insecure File Permission |
| **OWASP** | A02:2021 — Cryptographic Failures |
| **Affected Component** | `frontend/src/store/authStore.js` |
| **Affected Component** | `frontend/src/services/api.js` |

---

## Description

The JWT authentication token is stored in the browser's `localStorage` via the Zustand `persist` middleware. This makes the token accessible to any JavaScript running on the page, including malicious scripts injected through XSS vulnerabilities.

The token is persisted across browser sessions using Zustand's `persist` middleware with a `partialize` function that explicitly saves the token to `localStorage`:

```javascript
{
  name: 'auth-storage',
  partialize: (state) => ({
    user: state.user,
    token: state.token,          // <-- Token persisted to localStorage
    isAuthenticated: state.isAuthenticated,
  }),
}
```

Additionally, the Axios interceptor reads this token from the store and attaches it to every outgoing request via the `Authorization` header:

```javascript
config.headers.Authorization = `Bearer ${token}`;
```

---

## Risk Assessment

### Impact

| Impact | Description |
|--------|-------------|
| **Account Takeover** | Any XSS vulnerability can read the token from localStorage and exfiltrate it to an attacker-controlled server |
| **Session Hijacking** | The stolen token can be used to impersonate the victim user until the token expires |
| **Persistent Access** | Since the token has a 24-hour expiration, the attacker has a wide window of opportunity |
| **Full API Access** | The token grants access to all authenticated endpoints (user profile, orders, cart, etc.) |

### Likelihood

| Factor | Assessment |
|--------|------------|
| **Attack Complexity** | Low — requires only an XSS vulnerability (see Vulnerability #5) |
| **Prerequisites** | Any script injection point (review comments, product descriptions, etc.) |
| **Automation** | Trivial — `localStorage.getItem('auth-storage')` in any injected script |

### Risk Rating: **HIGH**

Combined with the **Stored XSS** vulnerability (Vulnerability #5), an attacker can inject a script that reads the JWT token from localStorage and sends it to an external server, achieving full account takeover.

---

## Root Cause Analysis

### Code Evidence

**File:** `frontend/src/store/authStore.js`

```javascript
{
  name: 'auth-storage',
  partialize: (state) => ({
    user: state.user,
    token: state.token,
    isAuthenticated: state.isAuthenticated,
  }),
}
```

**File:** `frontend/src/services/api.js`

```javascript
api.interceptors.request.use(
  (config) => {
    const token = useAuthStore.getState().token;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  ...
);
```

**Findings:**
1. **Token stored in localStorage** — Zustand's `persist` middleware saves the token to `localStorage` under the key `auth-storage`, which is readable by any JavaScript on the page.
2. **Token sent via Authorization header** — The Axios interceptor reads the token from the store and attaches it to every request, but the token's exposure in localStorage makes this moot.
3. **Long token expiration** — The JWT has a 24-hour expiration (`jwt.expiration: 86400000`), giving attackers a wide window to use stolen tokens.
4. **No token refresh mechanism** — There is no short-lived access token with a separate refresh token pattern.

---

## References

- **CWE-922:** https://cwe.mitre.org/data/definitions/922.html
- **OWASP JWT Security Cheatsheet:** https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_for_Java_Cheat_Sheet.html
- **OWASP HTML5 Security Cheatsheet (localStorage):** https://cheatsheetseries.owasp.org/cheatsheets/HTML5_Security_Cheat_Sheet.html#local_storage
