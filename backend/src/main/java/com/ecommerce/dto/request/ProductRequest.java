package com.ecommerce.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @Size(max = 50, message = "Brand must not exceed 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-\\.]*$", message = "Brand contains invalid characters")
    private String brand;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be positive")
    private BigDecimal price;

    private BigDecimal discountPrice;
    private int discountPercentage;

    @NotBlank(message = "Category ID is required")
    private String categoryId;

    private List<String> images;

    @Min(value = 0, message = "Stock quantity must be positive")
    private int stockQuantity;

    private boolean active;
    private boolean featured;

    @Size(max = 10, message = "Maximum 10 tags allowed")
    private List<@Size(max = 30, message = "Each tag must not exceed 30 characters") String> tags;

    @Size(max = 50, message = "Weight must not exceed 50 characters")
    private String weight;

    @Size(max = 50, message = "Dimensions must not exceed 50 characters")
    private String dimensions;

    @Size(max = 30, message = "Color must not exceed 30 characters")
    private String color;

    @Size(max = 100, message = "Material must not exceed 100 characters")
    private String material;

    @Size(max = 100, message = "Warranty must not exceed 100 characters")
    private String warranty;
}
