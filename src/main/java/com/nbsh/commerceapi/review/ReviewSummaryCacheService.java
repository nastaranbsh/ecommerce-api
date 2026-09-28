package com.nbsh.commerceapi.review;

import com.nbsh.commerceapi.common.cache.CacheNames;
import com.nbsh.commerceapi.common.exception.ResourceNotFoundException;
import com.nbsh.commerceapi.product.ProductRepository;
import com.nbsh.commerceapi.review.dto.ReviewSummaryResponse;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ReviewSummaryCacheService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    public ReviewSummaryCacheService(
            ReviewRepository reviewRepository,
            ProductRepository productRepository
    ) {
        this.reviewRepository =
                reviewRepository;

        this.productRepository =
                productRepository;
    }

    @Cacheable(
            cacheNames = CacheNames.REVIEW_SUMMARIES,
            key = "#productId"
    )
    public ReviewSummaryResponse getSummary(
            Long productId
    ) {

        ensureProductExists(productId);

        long count =
                reviewRepository
                        .countByProductId(
                                productId
                        );

        Double average =
                reviewRepository
                        .findAverageRatingByProductId(
                                productId
                        );

        BigDecimal averageRating =
                average == null
                        ? BigDecimal.ZERO
                        : BigDecimal
                        .valueOf(average)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        return new ReviewSummaryResponse(
                productId,
                count,
                averageRating
        );
    }

    @CacheEvict(
            cacheNames = CacheNames.REVIEW_SUMMARIES,
            key = "#productId"
    )
    public void evictSummary(
            Long productId
    ) {
        // Cache eviction is performed by Spring.
    }

    private void ensureProductExists(
            Long productId
    ) {

        if (!productRepository
                .existsById(productId)) {

            throw new ResourceNotFoundException(
                    "Product not found with id: "
                            + productId
            );
        }
    }
}