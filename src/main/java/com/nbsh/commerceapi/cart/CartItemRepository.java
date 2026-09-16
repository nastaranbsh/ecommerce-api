package com.nbsh.commerceapi.cart;

import org.springframework.data.jpa.repository.JpaRepository;

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

    void deleteAllByCartId(Long cartId);
}