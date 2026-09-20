package com.nbsh.commerceapi.order;

import com.nbsh.commerceapi.product.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "order_items")
public class OrderItem {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "product_id"
    )
    private Product product;

    @Column(
            name = "product_name",
            nullable = false,
            length = 150
    )
    private String productName;

    @Column(
            name = "product_sku",
            nullable = false,
            length = 100
    )
    private String productSku;

    @Column(
            name = "unit_price",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int quantity;

    @Column(
            name = "line_total",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal lineTotal;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    protected OrderItem() {
    }

    public OrderItem(
            Order order,
            Product product,
            String productName,
            String productSku,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal lineTotal
    ) {
        this.order = order;
        this.product = product;
        this.productName = productName;
        this.productSku = productSku;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
    }

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public Product getProduct() {
        return product;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductSku() {
        return productSku;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}