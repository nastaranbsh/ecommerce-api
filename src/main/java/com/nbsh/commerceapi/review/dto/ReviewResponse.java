package com.nbsh.commerceapi.review.dto;

import java.time.Instant;

public record ReviewResponse(
        Long id,
        Long productId,
        String reviewerName,
        int rating,
        String title,
        String comment,
        Instant createdAt,
        Instant updatedAt
) {
}