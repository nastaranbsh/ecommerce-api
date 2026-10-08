package com.nbsh.commerceapi.review;

import com.nbsh.commerceapi.support.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.*;

class ReviewRepositoryIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    TestDataFactory dataFactory;
    @Autowired
    ReviewRepository reviewRepository;

    @Test
    void databaseRejectsDuplicateUserProductReviews() {

        var user = dataFactory.createCustomer("unique@review.test");

        var product = dataFactory.createProduct(
                "Item",
                "UNIQUE-REVIEW",
                "10.00",
                1
        );

        reviewRepository.saveAndFlush(new Review(
                user,
                product,
                5,
                "First",
                "First review"
        ));

        assertThatThrownBy(() -> reviewRepository.saveAndFlush(new Review(
                user,
                product,
                4,
                "Second",
                "Second review"
        ))).isInstanceOf(DataIntegrityViolationException.class);
    }
}
