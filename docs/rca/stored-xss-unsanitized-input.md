# Root Cause Analysis — Stored XSS via Unsanitized Input

## Vulnerability Overview

| Attribute | Details |
|-----------|---------|
| **Vulnerability** | Stored Cross-Site Scripting (XSS) via Unsanitized User Input |
| **Severity** | High |
| **CWE** | CWE-79 — Improper Neutralization of Input During Web Page Generation |
| **OWASP** | A03:2021 — Injection |
| **Affected Components** | `ReviewService.java`, `ProductService.java`, `CategoryService.java` |
| **Affected Endpoints** | `POST /api/reviews`, `POST /api/admin/products`, `POST /api/admin/categories` |

---

## Description

The application stores user-supplied content (review comments, review titles, product descriptions, product names, category descriptions, user names) in MongoDB without any HTML sanitization. This content is then returned as-is in API responses. If the frontend renders these values as HTML (using `v-html` in Vue, `dangerouslySetInnerHTML` in React, or similar), any embedded scripts will execute in the browser of anyone viewing the page.

Since review endpoints (`GET /api/reviews/product/{productId}`) are publicly accessible without authentication, any visitor viewing an infected product page would execute the injected script.

---

## Risk Assessment

### Impact

| Impact | Description |
|--------|-------------|
| **Session/Token Theft** | Injected scripts can read authentication tokens from localStorage or cookies and exfiltrate them |
| **Account Takeover** | Stolen tokens enable full account impersonation |
| **Defacement** | Attackers can modify page content to display fake login forms or misleading information |
| **Malware Distribution** | Scripts can redirect users to malicious sites or trigger drive-by downloads |
| **Data Exfiltration** | Scripts can read and send any data visible on the page to an external server |

### Likelihood

| Factor | Assessment |
|--------|------------|
| **Attack Complexity** | Low — submit malicious content via any public API endpoint |
| **Prerequisites** | None — review creation is available to authenticated users, and product/category creation is available to admins |
| **Automation** | Trivial — a single POST request with script content in any text field |

### Risk Rating: **HIGH**

The attack is persistent — once injected, the malicious script executes every time any user views the affected page. Combined with the **JWT Exposure in Client Storage** (Vulnerability #2), an attacker can steal tokens directly from localStorage.

---

## Root Cause Analysis

### Code Evidence

**File:** `backend/src/main/java/com/ecommerce/service/ReviewService.java`

```java
review.setTitle(request.getTitle());       // Stored as-is
review.setComment(request.getComment());   // Stored as-is
```

**File:** `backend/src/main/java/com/ecommerce/service/ProductService.java`

```java
product.setDescription(request.getDescription()); // Stored as-is
product.setName(request.getName());               // Stored as-is
```

**File:** `backend/src/main/java/com/ecommerce/service/CategoryService.java`

```java
category.setDescription(request.getDescription()); // Stored as-is
```

**Findings:**
1. **No HTML sanitization** — There is no usage of `HtmlUtils`, `StringEscapeUtils`, OWASP Java HTML Sanitizer, or any equivalent library anywhere in the codebase.
2. **User input stored as-is** — Review title, review comment, product name, product description, category description, and user names are stored verbatim without any transformation.
3. **Publicly readable** — Review endpoints (`GET /api/reviews/**`) and product/category endpoints (`GET /api/products/**`, `GET /api/categories/**`) are publicly accessible without authentication per `SecurityConfig`.
4. **No Content-Security-Policy headers** — The `SecurityConfig` does not set CSP headers that would restrict inline script execution.

---

## References

- **CWE-79:** https://cwe.mitre.org/data/definitions/79.html
- **OWASP XSS Prevention Cheatsheet:** https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html
- **OWASP Java HTML Sanitizer:** https://github.com/OWASP/java-html-sanitizer
