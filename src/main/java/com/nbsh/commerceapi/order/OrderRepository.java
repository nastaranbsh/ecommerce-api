package com.nbsh.commerceapi.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository
        extends JpaRepository<Order, Long> {

    @EntityGraph(
            attributePaths = {
                    "items"
            }
    )
    Optional<Order> findByIdAndUserId(
            Long id,
            Long userId
    );

    @EntityGraph(
            attributePaths = {
                    "items"
            }
    )
    Optional<Order> findByOrderNumberAndUserId(
            String orderNumber,
            Long userId
    );

    Page<Order> findAllByUserId(
            Long userId,
            Pageable pageable
    );

    @EntityGraph(
            attributePaths = {
                    "items"
            }
    )
    Optional<Order> findWithItemsById(
            Long id
    );
}