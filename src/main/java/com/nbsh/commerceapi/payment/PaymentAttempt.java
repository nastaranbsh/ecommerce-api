package com.nbsh.commerceapi.payment;

import com.nbsh.commerceapi.order.Order;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payment_attempts")
public class PaymentAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "order_id",
            nullable = false
    )
    private Order order;

    @Column(
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 50
    )
    private PaymentStatus status;

    @Column(
            name = "gateway_reference",
            length = 100
    )
    private String gatewayReference;

    @Column(
            name = "failure_code",
            length = 100
    )
    private String failureCode;

    @Column(
            name = "failure_message",
            length = 500
    )
    private String failureMessage;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(
            name = "idempotency_key",
            nullable = false,
            unique = true,
            length = 100
    )
    private String idempotencyKey;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    protected PaymentAttempt() {
    }

    public PaymentAttempt(
            Order order,
            BigDecimal amount,
            String idempotencyKey
    ) {
        this.order = order;
        this.amount = amount;
        this.status = PaymentStatus.INITIATED;
        this.idempotencyKey = idempotencyKey;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();

        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getGatewayReference() {
        return gatewayReference;
    }

    public String getFailureCode() {
        return failureCode;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public Long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void markSucceeded(
            String gatewayReference
    ) {
        this.status =
                PaymentStatus.SUCCEEDED;

        this.gatewayReference =
                gatewayReference;

        this.failureCode = null;
        this.failureMessage = null;
    }

    public void markDeclined(
            String failureCode,
            String failureMessage
    ) {
        this.status =
                PaymentStatus.DECLINED;

        this.failureCode =
                failureCode;

        this.failureMessage =
                failureMessage;
    }

    public void markUnknown(
            String failureMessage
    ) {
        this.status =
                PaymentStatus.UNKNOWN;

        this.failureMessage =
                failureMessage;
    }
}