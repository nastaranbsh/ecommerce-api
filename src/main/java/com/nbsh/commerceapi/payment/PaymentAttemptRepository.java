package com.nbsh.commerceapi.payment;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface PaymentAttemptRepository
        extends JpaRepository<PaymentAttempt, Long> {

    boolean existsByOrderIdAndStatusIn(
            Long orderId,
            Collection<PaymentStatus> statuses
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select p
            from PaymentAttempt p
            where p.id = :id
            """)
    Optional<PaymentAttempt> findByIdForUpdate(
            @Param("id")
            Long id
    );
}