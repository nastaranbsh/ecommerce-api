package com.nbsh.commerceapi.user.dto;

import java.time.Instant;

public record AddressResponse(
        Long id,
        String label,
        String recipientName,
        String line1,
        String line2,
        String city,
        String stateOrProvince,
        String postalCode,
        String countryCode,
        String phone,
        boolean defaultAddress,
        Instant createdAt,
        Instant updatedAt
) {
}