# Pagination Endpoint Catalog — Dynamic Sort Parameters

## Endpoints with Dynamic Sort (Vulnerable)

| Endpoint | Sort Parameter | Default Sort | Max Page Size | Validation |
|----------|---------------|--------------|---------------|------------|
| `GET /api/products` | `sortBy`, `sortDir` | `createdAt` desc | None | ❌ None |

## Endpoints with Hardcoded Sort (Safe)

| Endpoint | Sort Logic | Max Page Size |
|----------|-----------|---------------|
| `GET /api/products/category/{categoryId}` | `createdAt` desc | None |
| `GET /api/products/featured` | `createdAt` desc | None |
| `GET /api/products/search` | `createdAt` desc | None |
| `GET /api/products/filter/price` | `createdAt` desc | None |
| `GET /api/products/filter/rating` | `createdAt` desc | None |
| `GET /api/reviews/product/{productId}` | `createdAt` desc | None |
| `GET /api/orders` | `createdAt` desc | None |
| `GET /api/admin/products` | `createdAt` desc | None |
| `GET /api/admin/orders` | `createdAt` desc | None |
| `GET /api/admin/users` | `createdAt` desc | None |

## Recommended Allowed Sort Fields for Products

| Field | Type | Safe? |
|-------|------|-------|
| `name` | String | ✅ Yes |
| `price` | BigDecimal | ✅ Yes |
| `rating` | Double | ✅ Yes |
| `createdAt` | Date | ✅ Yes |
| `updatedAt` | Date | ✅ Yes |
| `brand` | String | ✅ Yes |
| `password` | String | ❌ No |
| `roles` | List | ❌ No |
| `_id` | ObjectId | ❌ No |
