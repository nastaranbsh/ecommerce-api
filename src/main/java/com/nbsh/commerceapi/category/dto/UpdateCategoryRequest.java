package com.nbsh.commerceapi.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateCategoryRequest(

        @NotBlank(message = "Category name is required")
        @Size(max = 120, message = "Category name must not exceed 120 characters")
        String name,

        @NotBlank(message = "Category slug is required")
        @Size(max = 120, message = "Category slug must not exceed 120 characters")
        String slug,

        @Size(max = 5000, message = "Description must not exceed 5000 characters")
        String description,

        @NotNull(message = "Active status is required")
        Boolean active
) {
}