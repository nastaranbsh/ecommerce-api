package com.nbsh.commerceapi.product;

import com.nbsh.commerceapi.product.dto.ProductFilter;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Locale;

public final class ProductSpecification {

    private ProductSpecification() {
    }

    public static Specification<Product> withFilters(
            ProductFilter filter
    ) {

        Specification<Product> specification =
                Specification.unrestricted();

        if (filter.search() != null
                && !filter.search().isBlank()) {

            specification = specification.and(
                    nameOrDescriptionContains(filter.search())
            );
        }

        if (filter.categoryId() != null) {

            specification = specification.and(
                    hasCategory(filter.categoryId())
            );
        }

        if (filter.minPrice() != null) {

            specification = specification.and(
                    priceGreaterThanOrEqualTo(filter.minPrice())
            );
        }

        if (filter.maxPrice() != null) {

            specification = specification.and(
                    priceLessThanOrEqualTo(filter.maxPrice())
            );
        }

        if (filter.active() != null) {

            specification = specification.and(
                    hasActiveStatus(filter.active())
            );
        }

        return specification;
    }

    private static Specification<Product> nameOrDescriptionContains(
            String search
    ) {

        return (root, query, criteriaBuilder) -> {

            String pattern =
                    "%" + search
                            .trim()
                            .toLowerCase(Locale.ROOT)
                            + "%";

            return criteriaBuilder.or(

                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    root.get("name")
                            ),
                            pattern
                    ),

                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    root.get("description")
                            ),
                            pattern
                    )
            );
        };
    }

    private static Specification<Product> hasCategory(
            Long categoryId
    ) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("category").get("id"),
                        categoryId
                );
    }

    private static Specification<Product> priceGreaterThanOrEqualTo(
            BigDecimal minimum
    ) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(
                        root.get("price"),
                        minimum
                );
    }

    private static Specification<Product> priceLessThanOrEqualTo(
            BigDecimal maximum
    ) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(
                        root.get("price"),
                        maximum
                );
    }

    private static Specification<Product> hasActiveStatus(
            boolean active
    ) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("active"),
                        active
                );
    }
}