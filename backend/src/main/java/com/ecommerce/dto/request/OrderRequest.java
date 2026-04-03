package com.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    @NotNull(message = "Shipping address is required")
    private ShippingAddressDto shippingAddress;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingAddressDto {
        @NotBlank(message = "Full name is required")
        @Size(max = 100, message = "Full name must not exceed 100 characters")
        private String fullName;

        @NotBlank(message = "Phone is required")
        @Size(max = 20, message = "Phone must not exceed 20 characters")
        @Pattern(regexp = "^[+]?[0-9\\s\\-\\(\\)]*$", message = "Phone contains invalid characters")
        private String phone;

        @NotBlank(message = "Street is required")
        @Size(max = 500, message = "Street must not exceed 500 characters")
        private String street;

        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City must not exceed 100 characters")
        private String city;

        @NotBlank(message = "State is required")
        @Size(max = 100, message = "State must not exceed 100 characters")
        private String state;

        @NotBlank(message = "Zip code is required")
        @Size(max = 20, message = "Zip code must not exceed 20 characters")
        private String zipCode;

        @NotBlank(message = "Country is required")
        @Size(max = 100, message = "Country must not exceed 100 characters")
        private String country;
    }
}
