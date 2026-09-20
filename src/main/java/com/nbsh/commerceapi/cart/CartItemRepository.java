package com.nbsh.commerceapi.cart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartItemRepository
        extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartIdAndProductId(
            Long cartId,
            Long productId
    );

    Optional<CartItem> findByIdAndCartUserId(
            Long itemId,
            Long userId
    );

//    void deleteAllByCartId(Long cartId);
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            DELETE FROM CartItem ci
            WHERE ci.cart.id = :cartId
            """)
    int deleteAllByCartId(
            @Param("cartId") Long cartId
    );
}