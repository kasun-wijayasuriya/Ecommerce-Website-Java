package com.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
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
public class UpdateProfileRequest {
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    @Pattern(regexp = "^[^<>]*$", message = "Invalid characters in first name")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    @Pattern(regexp = "^[^<>]*$", message = "Invalid characters in last name")
    private String lastName;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Pattern(
            regexp = "^[0-9+\\-() ]*$",
            message = "Phone number contains invalid characters"
    )
    private String phone;

    @Size(max = 255, message = "Profile image URL must not exceed 255 characters")
    @Pattern(regexp = "^[^<>]*$", message = "Invalid characters in profile image")
    private String profileImage;

    @Size(max = 100, message = "Street must not exceed 100 characters")
    @Pattern(regexp = "^[^<>]*$", message = "Invalid characters in street")
    private String street;

    @Size(max = 100, message = "City must not exceed 100 characters")
    @Pattern(regexp = "^[^<>]*$", message = "Invalid characters in city")
    private String city;

    @Size(max = 100, message = "State must not exceed 100 characters")
    @Pattern(regexp = "^[^<>]*$", message = "Invalid characters in state")
    private String state;

    @Size(max = 20, message = "Zip code must not exceed 20 characters")
    @Pattern(regexp = "^[^<>]*$", message = "Invalid characters in zip code")
    private String zipCode;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    @Pattern(regexp = "^[^<>]*$", message = "Invalid characters in country")
    private String country;
}
