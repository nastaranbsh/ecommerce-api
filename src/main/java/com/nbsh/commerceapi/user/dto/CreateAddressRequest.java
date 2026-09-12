package com.nbsh.commerceapi.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAddressRequest(

        @Size(max = 100, message = "Label must not exceed 100 characters")
        String label,

        @NotBlank(message = "Recipient name is required")
        @Size(max = 200, message = "Recipient name must not exceed 200 characters")
        String recipientName,

        @NotBlank(message = "Address line 1 is required")
        @Size(max = 255, message = "Address line 1 must not exceed 255 characters")
        String line1,

        @Size(max = 255, message = "Address line 2 must not exceed 255 characters")
        String line2,

        @NotBlank(message = "City is required")
        @Size(max = 150, message = "City must not exceed 150 characters")
        String city,

        @Size(max = 150, message = "State or province must not exceed 150 characters")
        String stateOrProvince,

        @NotBlank(message = "Postal code is required")
        @Size(max = 50, message = "Postal code must not exceed 50 characters")
        String postalCode,

        @NotBlank(message = "Country code is required")
        @Size(
                min = 2,
                max = 2,
                message = "Country code must contain exactly 2 characters"
        )
        String countryCode,

        @Size(max = 50, message = "Phone must not exceed 50 characters")
        String phone,

        Boolean defaultAddress
) {
}