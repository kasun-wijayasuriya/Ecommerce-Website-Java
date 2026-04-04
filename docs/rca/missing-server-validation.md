# Root Cause Analysis — Missing Server-Side Validation

## Vulnerability Overview

| Attribute | Details |
|-----------|---------|
| **Vulnerability** | Missing Server-Side Input Validation on Multiple DTO Fields |
| **Severity** | Medium |
| **CWE** | CWE-20 — Improper Input Validation |
| **OWASP** | A03:2021 — Injection |
| **Affected Components** | `ReviewRequest.java`, `ProductRequest.java`, `CategoryRequest.java`, `CartItemRequest.java`, `OrderRequest.java` |

---

## Description

Multiple API endpoints accept user-supplied input without adequate server-side validation. While some fields have basic `@NotBlank` constraints, many fields lack maximum length limits, character pattern restrictions, or content validation. This allows clients to submit oversized payloads, malformed data, or potentially malicious content that bypasses frontend validation.

For example, the `ReviewRequest.title` field has no `@Size(max=...)` constraint, and `ProductRequest.description` has no length limit at all.

---

## Risk Assessment

### Impact

| Impact | Description |
|--------|-------------|
| **Data Integrity** | Overly long or malformed data can corrupt database records and break downstream processing |
| **Denial of Service** | Extremely large payloads (e.g., megabyte-long strings) can exhaust memory and storage |
| **Stored XSS** | Unvalidated script content can be stored and later rendered in the frontend (see Vulnerability #5) |
| **NoSQL Injection** | Improperly validated input passed to MongoDB queries may enable injection attacks |

### Likelihood

| Factor | Assessment |
|--------|------------|
| **Attack Complexity** | Low — send oversized or malformed payloads via any API client |
| **Prerequisites** | Authenticated access for most endpoints (reviews, cart, orders); admin access for products/categories |
| **Automation** | Trivial — automated scripts can send large payloads to all writable endpoints |

### Risk Rating: **MEDIUM**

---

## Root Cause Analysis

### Code Evidence

**File:** `backend/src/main/java/com/ecommerce/dto/request/ReviewRequest.java`

```java
@NotBlank(message = "Title is required")
private String title;  // No @Size(max=...)

@NotBlank(message = "Comment is required")
private String comment;  // No @Size(max=...)
```

**File:** `backend/src/main/java/com/ecommerce/dto/request/ProductRequest.java`

```java
private String description;  // No @Size(max=...)
private String brand;        // No @Size(max=...)
private List<String> tags;   // No size limits on list or elements
private String color, material, warranty; // No validation at all
```

**File:** `backend/src/main/java/com/ecommerce/dto/request/CategoryRequest.java`

```java
private String description;  // No @Size(max=...)
```

**Findings:**
1. **No maximum length constraints** — Many text fields lack `@Size(max=N)` annotations, allowing arbitrarily long input.
2. **No pattern validation** — Fields like `brand`, `color`, `tags` have no `@Pattern` restrictions on allowed characters.
3. **No list size limits** — The `tags` list in `ProductRequest` has no maximum element count or per-element size limit.
4. **No global request size limit** — The application does not enforce a maximum request body size in configuration.

---

## References

- **CWE-20:** https://cwe.mitre.org/data/definitions/20.html
- **OWASP Input Validation Cheatsheet:** https://cheatsheetseries.owasp.org/cheatsheets/Input_Validation_Cheat_Sheet.html
