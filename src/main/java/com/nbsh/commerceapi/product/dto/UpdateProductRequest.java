package com.nbsh.commerceapi.product.dto;

import java.math.BigDecimal;

public record UpdateProductRequest(
        String name,
        String description,
        BigDecimal price,
        String sku,
        Boolean active
) {
}