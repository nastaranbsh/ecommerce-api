package com.nbsh.commerceapi.review;

import com.nbsh.commerceapi.common.api.PageResponse;
import com.nbsh.commerceapi.common.exception.InvalidRequestException;
import com.nbsh.commerceapi.review.dto.CreateReviewRequest;
import com.nbsh.commerceapi.review.dto.ReviewResponse;
import com.nbsh.commerceapi.review.dto.ReviewSummaryResponse;
import com.nbsh.commerceapi.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping(
        "/api/v1/products/{productId}/reviews"
)
public class ReviewController {

    private static final int MAX_PAGE_SIZE = 100;

    private final ReviewService reviewService;
    private final CurrentUser currentUser;

    public ReviewController(
            ReviewService reviewService,
            CurrentUser currentUser
    ) {
        this.reviewService =
                reviewService;

        this.currentUser =
                currentUser;
    }

    @GetMapping
    public PageResponse<ReviewResponse>
    getReviews(
            @PathVariable Long productId,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size
    ) {

        validatePagination(
                page,
                size
        );

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );

        return reviewService
                .getReviewsForProduct(
                        productId,
                        pageable
                );
    }

    @GetMapping("/summary")
    public ReviewSummaryResponse getSummary(
            @PathVariable Long productId
    ) {

        return reviewService
                .getSummary(productId);
    }

    @PostMapping
    public ResponseEntity<ReviewResponse>
    createReview(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long productId,
            @Valid
            @RequestBody
            CreateReviewRequest request
    ) {

        Long userId =
                currentUser.getUserId(jwt);

        ReviewResponse review =
                reviewService
                        .createReview(
                                userId,
                                productId,
                                request
                        );

        return ResponseEntity
                .created(
                        URI.create(
                                "/api/v1/products/"
                                        + productId
                                        + "/reviews/"
                                        + review.id()
                        )
                )
                .body(review);
    }

    private void validatePagination(
            int page,
            int size
    ) {

        if (page < 0) {
            throw new InvalidRequestException(
                    "Page number must be greater than or equal to 0"
            );
        }

        if (size < 1
                || size > MAX_PAGE_SIZE) {

            throw new InvalidRequestException(
                    "Page size must be between 1 and "
                            + MAX_PAGE_SIZE
            );
        }
    }
}