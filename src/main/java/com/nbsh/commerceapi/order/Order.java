package com.nbsh.commerceapi.order;

import com.nbsh.commerceapi.common.exception.InvalidOrderStateException;
import com.nbsh.commerceapi.user.User;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "order_number",
            nullable = false,
            unique = true,
            length = 50
    )
    private String orderNumber;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 50
    )
    private OrderStatus status;

    @Column(
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal subtotal;

    @Column(
            name = "total_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal totalAmount;

    @Version
    @Column(nullable = false)
    private Long version;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderItem> items =
            new ArrayList<>();

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

    protected Order() {
    }

    public Order(
            String orderNumber,
            User user,
            BigDecimal subtotal,
            BigDecimal totalAmount
    ) {
        this.orderNumber = orderNumber;
        this.user = user;
        this.subtotal = subtotal;
        this.totalAmount = totalAmount;
        this.status = OrderStatus.PENDING;
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

    public String getOrderNumber() {
        return orderNumber;
    }

    public User getUser() {
        return user;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Long getVersion() {
        return version;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void addItem(
            OrderItem item
    ) {
        items.add(item);
    }

    public void changeStatus(
            OrderStatus newStatus
    ) {

        if (!canTransitionTo(newStatus)) {
            throw new InvalidOrderStateException(
                    "Invalid order status transition from "
                            + status
                            + " to "
                            + newStatus
            );
        }

        this.status = newStatus;
    }

    private boolean canTransitionTo(
            OrderStatus newStatus
    ) {

        return switch (status) {

            case PENDING ->
                    newStatus == OrderStatus.PAID
                            || newStatus == OrderStatus.PAYMENT_FAILED
                            || newStatus == OrderStatus.CANCELLED;

            case PAID ->
                    newStatus == OrderStatus.PROCESSING
                            || newStatus == OrderStatus.REFUNDED
                            || newStatus == OrderStatus.CANCELLED;

            case PAYMENT_FAILED ->
                    newStatus == OrderStatus.CANCELLED;

            case PROCESSING ->
                    newStatus == OrderStatus.SHIPPED
                            || newStatus == OrderStatus.CANCELLED;

            case SHIPPED ->
                    newStatus == OrderStatus.DELIVERED;

            case DELIVERED ->
                    newStatus == OrderStatus.REFUNDED;

            case CANCELLED,
                 REFUNDED ->
                    false;
        };
    }
}