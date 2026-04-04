package com.ecommerce.dto.request;

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
    @Size(max = 50, message = "First name must not exceed 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s\\-'\\.]*$", message = "First name contains invalid characters")
    private String firstName;

    @Size(max = 50, message = "Last name must not exceed 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s\\-'\\.]*$", message = "Last name contains invalid characters")
    private String lastName;
    private String phone;
    private String profileImage;
    
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
