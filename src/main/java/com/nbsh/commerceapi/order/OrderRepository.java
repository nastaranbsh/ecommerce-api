package com.nbsh.commerceapi.order;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select o
        from Order o
        where o.id = :orderId
          and o.user.id = :userId
        """)
    Optional<Order> findByIdAndUserIdForUpdate(
            @Param("orderId") Long orderId,
            @Param("userId") Long userId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select o
        from Order o
        where o.id = :orderId
        """)
    Optional<Order> findByIdForUpdate(
            @Param("orderId") Long orderId
    );


    @Query("""
        select (count(o) > 0)
        from Order o
        join o.items oi
        where o.user.id = :userId
          and oi.product.id = :productId
          and o.status in :statuses
        """)
    boolean existsPurchasedProduct(
            @Param("userId")
            Long userId,
            @Param("productId")
            Long productId,
            @Param("statuses")
            Collection<OrderStatus> statuses
    );
}