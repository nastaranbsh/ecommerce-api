package com.nbsh.commerceapi.cart;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository
        extends JpaRepository<Cart, Long> {

    @EntityGraph(
            attributePaths = {
                    "items",
                    "items.product",
                    "items.product.category"
            }
    )
    Optional<Cart> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select c
            from Cart c
            where c.user.id = :userId
            """)
    Optional<Cart> findByUserIdForUpdate(
            @Param("userId")
            Long userId
    );
}