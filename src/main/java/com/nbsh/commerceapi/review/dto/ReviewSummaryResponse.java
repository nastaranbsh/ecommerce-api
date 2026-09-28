package com.nbsh.commerceapi.review.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public record ReviewSummaryResponse(
        Long productId,
        long reviewCount,
        BigDecimal averageRating
) implements Serializable {
}