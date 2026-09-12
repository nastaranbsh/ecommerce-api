package com.nbsh.commerceapi.product.dto;

import java.math.BigDecimal;

public record ProductFilter(
        String search,
        Long categoryId,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Boolean active
) {
}