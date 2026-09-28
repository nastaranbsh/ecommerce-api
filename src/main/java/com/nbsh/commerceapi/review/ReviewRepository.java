package com.nbsh.commerceapi.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReviewRepository
        extends JpaRepository<Review, Long> {

    boolean existsByUserIdAndProductId(
            Long userId,
            Long productId
    );

    @EntityGraph(
            attributePaths = {
                    "user"
            }
    )
    Page<Review> findAllByProductId(
            Long productId,
            Pageable pageable
    );

    @EntityGraph(
            attributePaths = {
                    "user",
                    "product"
            }
    )
    Optional<Review> findByIdAndUserId(
            Long reviewId,
            Long userId
    );

    @EntityGraph(
            attributePaths = {
                    "user",
                    "product"
            }
    )
    Optional<Review> findWithDetailsById(
            Long reviewId
    );

    long countByProductId(
            Long productId
    );

    @Query("""
            select avg(r.rating)
            from Review r
            where r.product.id = :productId
            """)
    Double findAverageRatingByProductId(
            @Param("productId")
            Long productId
    );

}