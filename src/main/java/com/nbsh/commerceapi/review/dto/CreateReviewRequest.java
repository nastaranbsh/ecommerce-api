package com.nbsh.commerceapi.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateReviewRequest(

        @Min(
                value = 1,
                message = "Rating must be at least 1"
        )
        @Max(
                value = 5,
                message = "Rating must not exceed 5"
        )
        int rating,

        @Size(
                max = 150,
                message = "Title must not exceed 150 characters"
        )
        String title,

        @NotBlank(
                message = "Comment is required"
        )
        @Size(
                max = 5000,
                message = "Comment must not exceed 5000 characters"
        )
        String comment

) {
}
