# XSS Field Catalog — User-Input Fields at Risk

## High Risk (Publicly Readable)

| Field | Model | Service | Endpoint | Auth Required to Read |
|-------|-------|---------|----------|----------------------|
| `review.title` | Review | ReviewService | `GET /api/reviews/product/{id}` | No |
| `review.comment` | Review | ReviewService | `GET /api/reviews/product/{id}` | No |
| `product.name` | Product | ProductService | `GET /api/products/**` | No |
| `product.description` | Product | ProductService | `GET /api/products/**` | No |
| `product.brand` | Product | ProductService | `GET /api/products/**` | No |
| `category.name` | Category | CategoryService | `GET /api/categories/**` | No |
| `category.description` | Category | CategoryService | `GET /api/categories/**` | No |

## Medium Risk (Authenticated Read)

| Field | Model | Service | Endpoint | Auth Required to Read |
|-------|-------|---------|----------|----------------------|
| `user.firstName` | User | UserService | `GET /api/users/profile` | Yes |
| `user.lastName` | User | UserService | `GET /api/users/profile` | Yes |
| `user.phone` | User | UserService | `GET /api/users/profile` | Yes |
| `review.userName` | Review | ReviewService | `GET /api/reviews/product/{id}` | No |

## Injection Vectors

```html
<script>alert('xss')</script>
<img src=x onerror=alert(1)>
<svg onload=alert(1)>
<iframe src="javascript:alert(1)">
<body onload=alert(1)>
<input onfocus=alert(1) autofocus>
<a href="javascript:alert(1)">click</a>
<div style="background:url(javascript:alert(1))">
```
