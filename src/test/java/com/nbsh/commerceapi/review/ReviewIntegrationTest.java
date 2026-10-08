package com.nbsh.commerceapi.review;

import com.nbsh.commerceapi.support.*;
import com.nbsh.commerceapi.review.dto.CreateReviewRequest;
import com.nbsh.commerceapi.order.*;
import com.nbsh.commerceapi.common.exception.ReviewNotAllowedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.*;

class ReviewIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    TestDataFactory dataFactory;

    @Autowired
    ReviewService reviewService;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ReviewRepository reviewRepository;

    @Test
    void pendingOrderDoesNotAllowReview() {
        var user = dataFactory.createCustomer("pending@review.test");

        var product = dataFactory.createProduct(
                "Item",
                "REVIEW-PEND",
                "10.00",
                1
        );

        dataFactory.createOrder(
                user,
                product,
                OrderStatus.PENDING
        );

        assertThatThrownBy(() -> reviewService.createReview(
                user.getId(),
                product.getId(),
                new CreateReviewRequest(
                        5,
                        "Great",
                        "Excellent product"
                )
        )).isInstanceOf(ReviewNotAllowedException.class);
    }

    @Test
    void paidOrderAllowsReview() {
        var user = dataFactory.createCustomer("paid@review.test");

        var product = dataFactory.createProduct(
                "Item",
                "REVIEW-PAID",
                "10.00",
                1
        );

        dataFactory.createOrder(
                user,
                product,
                OrderStatus.PAID
        );

        reviewService.createReview(
                user.getId(),
                product.getId(),
                new CreateReviewRequest(
                        5,
                        "Great",
                        "Excellent product"
                )
        );

        assertThat(reviewRepository.existsByUserIdAndProductId(
                user.getId(),
                product.getId()
        )).isTrue();
    }

//    Authorization Test
//    Ownership Test
}
