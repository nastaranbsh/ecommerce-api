package com.nbsh.commerceapi.product.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record UpdateProductRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 150, message = "Name must not exceed 150 characters")
        String name,

        @Size(max = 5000, message = "Description must not exceed 5000 characters")
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(
                value = "0.00",
                inclusive = true,
                message = "Price must be greater than or equal to 0"
        )
        @Digits(
                integer = 10,
                fraction = 2,
                message = "Price must have at most 10 integer digits and 2 decimal places"
        )
        BigDecimal price,

        @NotBlank(message = "SKU is required")
        @Size(max = 100, message = "SKU must not exceed 100 characters")
        String sku,

        @NotNull(message = "Active status is required")
        Boolean active,

        @NotNull(message = "Category is required")
        Long categoryId
) {
}