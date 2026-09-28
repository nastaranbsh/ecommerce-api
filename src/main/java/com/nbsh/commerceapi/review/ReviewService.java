package com.nbsh.commerceapi.review;

import com.nbsh.commerceapi.common.api.PageResponse;
import com.nbsh.commerceapi.common.exception.ResourceConflictException;
import com.nbsh.commerceapi.common.exception.ResourceNotFoundException;
import com.nbsh.commerceapi.common.exception.ReviewNotAllowedException;
import com.nbsh.commerceapi.order.OrderRepository;
import com.nbsh.commerceapi.order.OrderStatus;
import com.nbsh.commerceapi.product.Product;
import com.nbsh.commerceapi.product.ProductRepository;
import com.nbsh.commerceapi.review.dto.CreateReviewRequest;
import com.nbsh.commerceapi.review.dto.ReviewResponse;
import com.nbsh.commerceapi.review.dto.ReviewSummaryResponse;
import com.nbsh.commerceapi.review.dto.UpdateReviewRequest;
import com.nbsh.commerceapi.user.User;
import com.nbsh.commerceapi.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;

@Service
public class ReviewService {

    private static final Set<OrderStatus>
            PURCHASED_ORDER_STATUSES =
            Set.of(
                    OrderStatus.PAID,
                    OrderStatus.PROCESSING,
                    OrderStatus.SHIPPED,
                    OrderStatus.DELIVERED,
                    OrderStatus.REFUNDED
            );

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            OrderRepository orderRepository
    ) {
        this.reviewRepository =
                reviewRepository;

        this.productRepository =
                productRepository;

        this.userRepository =
                userRepository;

        this.orderRepository =
                orderRepository;
    }

    @Transactional
    public ReviewResponse createReview(
            Long userId,
            Long productId,
            CreateReviewRequest request
    ) {

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found with id: "
                                                + userId
                                )
                        );

        Product product =
                productRepository
                        .findById(productId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Product not found with id: "
                                                + productId
                                )
                        );

        if (reviewRepository
                .existsByUserIdAndProductId(
                        userId,
                        productId
                )) {

            throw new ResourceConflictException(
                    "You have already reviewed this product"
            );
        }

        boolean purchased =
                orderRepository
                        .existsPurchasedProduct(
                                userId,
                                productId,
                                PURCHASED_ORDER_STATUSES
                        );

        if (!purchased) {
            throw new ReviewNotAllowedException(
                    "You can only review products you have purchased"
            );
        }

        Review review =
                new Review(
                        user,
                        product,
                        request.rating(),
                        normalizeNullable(
                                request.title()
                        ),
                        request.comment()
                                .trim()
                );

        Review saved =
                reviewRepository.save(review);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse>
    getReviewsForProduct(
            Long productId,
            Pageable pageable
    ) {

        ensureProductExists(
                productId
        );

        Page<Review> page =
                reviewRepository
                        .findAllByProductId(
                                productId,
                                pageable
                        );

        List<ReviewResponse> content =
                page.getContent()
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    @Transactional(readOnly = true)
    public ReviewSummaryResponse getSummary(
            Long productId
    ) {

        ensureProductExists(
                productId
        );

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
                        : BigDecimal.valueOf(
                                average
                        )
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

    @Transactional
    public ReviewResponse updateOwnReview(
            Long userId,
            Long reviewId,
            UpdateReviewRequest request
    ) {

        Review review =
                reviewRepository
                        .findByIdAndUserId(
                                reviewId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Review not found with id: "
                                                + reviewId
                                )
                        );

        review.update(
                request.rating(),
                normalizeNullable(
                        request.title()
                ),
                request.comment()
                        .trim()
        );

        return toResponse(review);
    }

    @Transactional
    public void deleteOwnReview(
            Long userId,
            Long reviewId
    ) {

        Review review =
                reviewRepository
                        .findByIdAndUserId(
                                reviewId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Review not found with id: "
                                                + reviewId
                                )
                        );

        reviewRepository.delete(review);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteReviewAsAdmin(
            Long reviewId
    ) {

        Review review =
                reviewRepository
                        .findWithDetailsById(
                                reviewId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Review not found with id: "
                                                + reviewId
                                )
                        );

        reviewRepository.delete(review);
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

    private ReviewResponse toResponse(
            Review review
    ) {

        return new ReviewResponse(
                review.getId(),
                review.getProduct()
                        .getId(),
                reviewerName(
                        review.getUser()
                ),
                review.getRating(),
                review.getTitle(),
                review.getComment(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }

    private String reviewerName(
            User user
    ) {

        String lastName =
                user.getLastName();

        String lastInitial =
                lastName == null
                        || lastName.isBlank()
                        ? ""
                        : " "
                        + Character.toUpperCase(
                        lastName.charAt(0)
                )
                        + ".";

        return user.getFirstName()
                + lastInitial;
    }

    private String normalizeNullable(
            String value
    ) {

        if (value == null) {
            return null;
        }

        String trimmed =
                value.trim();

        return trimmed.isEmpty()
                ? null
                : trimmed;
    }
}