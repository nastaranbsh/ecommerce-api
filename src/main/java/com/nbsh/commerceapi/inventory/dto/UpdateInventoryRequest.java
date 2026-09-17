package com.nbsh.commerceapi.inventory.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateInventoryRequest(

        @NotNull(
                message = "Available quantity is required"
        )
        @Min(
                value = 0,
                message = "Available quantity cannot be negative"
        )
        @Max(
                value = 1_000_000,
                message = "Available quantity is too large"
        )
        Integer quantityAvailable

) {
}