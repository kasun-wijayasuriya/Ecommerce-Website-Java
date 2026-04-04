# Root Cause Analysis — Unrestricted Pagination and Sorting

## Vulnerability Overview

| Attribute | Details |
|-----------|---------|
| **Vulnerability** | Unrestricted Pagination and Sorting Parameters |
| **Severity** | Medium |
| **CWE** | CWE-770 — Allocation of Resources Without Limits or Throttling |
| **OWASP** | A04:2021 — Insecure Design |
| **Affected Endpoint** | `GET /api/products` (primary), other paginated endpoints |
| **Affected Component** | `ProductController.java` |

---

## Description

The `GET /api/products` endpoint accepts a `sortBy` query parameter that is passed directly to Spring Data's `Sort.by(sortBy)` without any whitelist validation. This allows clients to sort by any field in the `Product` document, including internal or sensitive fields. Additionally, there is no maximum page size limit, allowing clients to request arbitrarily large result sets.

```java
Sort sort = sortDir.equalsIgnoreCase("asc")
        ? Sort.by(sortBy).ascending()     // No whitelist validation
        : Sort.by(sortBy).descending();
```

---

## Risk Assessment

### Impact

| Impact | Description |
|--------|-------------|
| **Server Errors** | Sorting by non-existent or nested fields can trigger MongoDB exceptions, leading to 500 errors |
| **Denial of Service** | Sorting by computationally expensive fields or requesting extremely large page sizes can degrade database query performance |
| **Schema Enumeration** | Error responses from invalid sort fields may reveal internal database structure and field names |
| **Business Logic Bypass** | Sorting by unexpected fields could expose unintended data ordering |

### Likelihood

| Factor | Assessment |
|--------|------------|
| **Attack Complexity** | Low — modify query parameters in any HTTP request |
| **Prerequisites** | None — the products endpoint is publicly accessible |
| **Automation** | Trivial — automated scripts can iterate through field names or send large page sizes |

### Risk Rating: **MEDIUM**

---

## Root Cause Analysis

### Code Evidence

**File:** `backend/src/main/java/com/ecommerce/controller/ProductController.java`

```java
@GetMapping
public ResponseEntity<Page<ProductResponse>> getProducts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDir) {

    Sort sort = sortDir.equalsIgnoreCase("asc")
            ? Sort.by(sortBy).ascending()     // <-- No whitelist validation
            : Sort.by(sortBy).descending();

    Pageable pageable = PageRequest.of(page, size, sort);
    // ...
}
```

**Findings:**
1. **No sort field whitelist** — The `sortBy` parameter is used directly without checking against an allowed list of fields. An attacker could supply arbitrary field names.
2. **No maximum page size** — The `size` parameter has no upper bound. A client could request `size=1000000`, potentially causing memory exhaustion or slow queries.
3. **Other paginated endpoints** — `ReviewController`, `OrderController`, and `AdminController` also use pagination but with hardcoded safe sort fields. Only `ProductController` accepts dynamic sort input.

---

## References

- **CWE-770:** https://cwe.mitre.org/data/definitions/770.html
- **OWASP Pagination Security:** https://cheatsheetseries.owasp.org/cheatsheets/Query_Parameter_and_Form_Parameter_Manipulation_Prevention_Cheat_Sheet.html
