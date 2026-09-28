package com.nbsh.commerceapi.review;

import com.nbsh.commerceapi.review.dto.ReviewResponse;
import com.nbsh.commerceapi.review.dto.UpdateReviewRequest;
import com.nbsh.commerceapi.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/reviews")
public class MeReviewController {

    private final ReviewService reviewService;
    private final CurrentUser currentUser;

    public MeReviewController(
            ReviewService reviewService,
            CurrentUser currentUser
    ) {
        this.reviewService =
                reviewService;

        this.currentUser =
                currentUser;
    }

    @PutMapping("/{reviewId}")
    public ReviewResponse updateReview(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long reviewId,
            @Valid
            @RequestBody
            UpdateReviewRequest request
    ) {

        Long userId =
                currentUser.getUserId(jwt);

        return reviewService
                .updateOwnReview(
                        userId,
                        reviewId,
                        request
                );
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void>
    deleteReview(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long reviewId
    ) {

        Long userId =
                currentUser.getUserId(jwt);

        reviewService.deleteOwnReview(
                userId,
                reviewId
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}