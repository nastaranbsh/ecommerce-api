package com.nbsh.commerceapi.review;

import com.nbsh.commerceapi.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/v1/admin/reviews")
public class AdminReviewController {

    private final ReviewService reviewService;

    public AdminReviewController(
            ReviewService reviewService
    ) {
        this.reviewService =
                reviewService;
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void>
    deleteReview(
            @PathVariable Long reviewId
    ) {

        reviewService
                .deleteReviewAsAdmin(
                        reviewId
                );

        return ResponseEntity
                .noContent()
                .build();
    }
}