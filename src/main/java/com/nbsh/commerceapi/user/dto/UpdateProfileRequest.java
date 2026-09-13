package com.nbsh.commerceapi.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(
                max = 320,
                message = "Email must not exceed 320 characters"
        )
        String email,

        @NotBlank(message = "First name is required")
        @Size(
                max = 100,
                message = "First name must not exceed 100 characters"
        )
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(
                max = 100,
                message = "Last name must not exceed 100 characters"
        )
        String lastName
) {
}