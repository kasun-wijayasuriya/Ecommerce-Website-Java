# DTO Validation Catalog — Fields Missing Validation Constraints

## ReviewRequest.java

| Field | Current Validation | Missing Validation | Recommended Constraint |
|-------|-------------------|-------------------|----------------------|
| `title` | `@NotBlank` | No max length | `@Size(max = 200)` |
| `comment` | `@NotBlank` | No max length | `@Size(max = 2000)` |
| `rating` | `@Min(1)`, `@Max(5)` | — | ✅ Complete |
| `productId` | `@NotBlank` | — | ✅ Complete |

## ProductRequest.java

| Field | Current Validation | Missing Validation | Recommended Constraint |
|-------|-------------------|-------------------|----------------------|
| `name` | `@NotBlank` | No max length | `@Size(max = 200)` |
| `description` | None | No max length | `@Size(max = 5000)` |
| `brand` | None | No max length, no pattern | `@Size(max = 50)`, `@Pattern` |
| `tags` | None | No list size, no element size | `@Size(max = 10)`, elements `@Size(max = 30)` |
| `price` | `@NotNull`, `@Min(0)` | — | ✅ Complete |
| `categoryId` | `@NotBlank` | — | ✅ Complete |
| `color` | None | No max length | `@Size(max = 30)` |
| `material` | None | No max length | `@Size(max = 100)` |
| `warranty` | None | No max length | `@Size(max = 100)` |
| `weight` | None | No max length | `@Size(max = 50)` |
| `dimensions` | None | No max length | `@Size(max = 50)` |

## CategoryRequest.java

| Field | Current Validation | Missing Validation | Recommended Constraint |
|-------|-------------------|-------------------|----------------------|
| `name` | `@NotBlank` | No max length | `@Size(max = 100)` |
| `description` | None | No max length | `@Size(max = 1000)` |

## CartItemRequest.java

| Field | Current Validation | Missing Validation | Recommended Constraint |
|-------|-------------------|-------------------|----------------------|
| `productId` | `@NotBlank` | — | ✅ Complete |
| `quantity` | `@Min(1)` | No max value | `@Max(999)` |

## OrderRequest.java

| Field | Current Validation | Missing Validation | Recommended Constraint |
|-------|-------------------|-------------------|----------------------|
| `shippingAddress.fullName` | `@NotBlank` | No max length | `@Size(max = 100)` |
| `shippingAddress.phone` | `@NotBlank` | No max length, no pattern | `@Size(max = 20)`, `@Pattern` |
| `shippingAddress.street` | `@NotBlank` | No max length | `@Size(max = 500)` |
| `shippingAddress.city` | `@NotBlank` | No max length | `@Size(max = 100)` |
| `shippingAddress.state` | `@NotBlank` | No max length | `@Size(max = 100)` |
| `shippingAddress.zipCode` | `@NotBlank` | No max length | `@Size(max = 20)` |
| `shippingAddress.country` | `@NotBlank` | No max length | `@Size(max = 100)` |
| `paymentMethod` | `@NotBlank` | — | ✅ Complete |
