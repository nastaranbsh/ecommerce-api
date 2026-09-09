package com.nbsh.commerceapi.product.dto;

import java.math.BigDecimal;

public record CreateProductRequest(
        String name,
        String description,
        BigDecimal price,
        String sku,
        Boolean active
) {
}